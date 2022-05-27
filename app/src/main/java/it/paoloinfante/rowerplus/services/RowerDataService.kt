package it.paoloinfante.rowerplus.services

import android.app.Service
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import it.paoloinfante.rowerplus.models.RowerPull
import it.paoloinfante.rowerplus.models.TimerData
import it.paoloinfante.rowerplus.models.events.UsbServiceConnectionStatusEvent
import it.paoloinfante.rowerplus.models.events.UsbServicePermissionRequiredEvent
import it.paoloinfante.rowerplus.models.events.UsbServiceTimerUpdateEvent
import it.paoloinfante.rowerplus.receiver.RowerDataBroadcastReceiver
import it.paoloinfante.rowerplus.repositories.UsbServiceRepository
import it.paoloinfante.rowerplus.serial.UsbSerialConnectionManager
import it.paoloinfante.rowerplus.serial.UsbSerialProtocol
import it.paoloinfante.rowerplus.usb.ErgometerDeviceListener
import it.paoloinfante.rowerplus.usb.UsbDeviceProtocol
import it.paoloinfante.rowerplus.utils.RowerDataParser
import it.paoloinfante.rowerplus.utils.Stopwatch
import kotlinx.coroutines.*
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class RowerDataService : Service(), ErgometerDeviceListener,
    UsbSerialConnectionManager.Listener {
    companion object {
        private const val TAG = "RowerDataService"
    }

    private var ROWS_PER_CALORIE: Float = 0f
    private var METERS_PER_ROW: Float = 0f

    @Inject
    lateinit var usbServiceRepository: UsbServiceRepository

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    private var deviceVid: Int = 0
    private var devicePid: Int = 0

    //private val usbConnectionManager = UsbSerialConnectionManager(this, this)
    private val usbHidConnectionManager = UsbSerialConnectionManager(this, this)
    private lateinit var rowerDataParser: RowerDataParser

    //private var rowerSerialMcu: RowerSerialMcu? = null
    private var usbProtocol: UsbSerialProtocol? = null
    private var workoutStatus: WorkoutStatus? = null

    private val elapsedTimeStopwatch = Stopwatch()
    private val localBinder = LocalBinder()

    private var stopWatchPauseTask = Handler(Looper.getMainLooper())
    private var connectionRetryHandler: Handler? = null
    private var timerUpdater: Timer? = null

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()

        deviceVid = resources.getInteger(R.integer.custom_usb_vid)
        devicePid = resources.getInteger(R.integer.custom_usb_pid)

        val rowsPerCaloriesTyped = TypedValue()
        resources.getValue(R.dimen.rows_per_calorie, rowsPerCaloriesTyped, true)
        ROWS_PER_CALORIE = rowsPerCaloriesTyped.float

        val metersPerRowTyped = TypedValue()
        resources.getValue(R.dimen.meters_per_row, metersPerRowTyped, true)
        METERS_PER_ROW = metersPerRowTyped.float

        rowerDataParser = RowerDataParser(ROWS_PER_CALORIE, METERS_PER_ROW)
    }

    override fun onBind(p0: Intent?): IBinder {
        workoutStatus = WorkoutStatus(null, 0, 0, 0f, 0f, 0, 0f, 0f, null)

        return localBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        timerUpdater?.cancel()
        usbProtocol?.stop()
        connectionRetryHandler?.removeCallbacksAndMessages(null)

        return false
    }

    override fun onDestroy() {
        ioScope.cancel()
    }

    override fun onDeviceDataReceived(pull: RowerPull) {
        Log.d(TAG, "onSerialDataReceived: Received data from ergometer $pull")

        elapsedTimeStopwatch.start()
        stopWatchPauseTask.removeCallbacks(pauseStopwatch)
        stopWatchPauseTask.postDelayed(
            pauseStopwatch,
            resources.getInteger(R.integer.rowing_timer_timeout_ms).toLong()
        )

        rowerDataParser.parseData(pull, workoutStatus!!, elapsedTimeStopwatch)

        persistStatus(workoutStatus!!)

        if (timerUpdater == null) {
            timerUpdater = Timer()
            timerUpdater!!.scheduleAtFixedRate(
                timerTask,
                0,
                resources.getInteger(R.integer.data_update_timer_interval_ms).toLong()
            )
        }
    }

    private fun persistStatus(status: WorkoutStatus) {
        ioScope.launch {
            usbServiceRepository.emitWorkoutStatus(status)
        }
    }

    override fun onDeviceReadError(e: Exception?) {
        sendConnectionStatus(false)

        retryConnection()
    }

    private val pauseStopwatch = Runnable { elapsedTimeStopwatch.stop() }

    private val timerTask = object : TimerTask() {
        override fun run() {
            ioScope.launch {
                usbServiceRepository.emitEvent(
                    UsbServiceTimerUpdateEvent(
                        TimerData(
                            elapsedTimeStopwatch.elapsedSeconds.toInt(),
                            workoutStatus!!.calories,
                            workoutStatus!!.distance,
                            workoutStatus!!.rowsCount,
                            workoutStatus!!.currentRPM,
                            workoutStatus!!.currentSecsFor500M
                        )
                    )
                )
            }
            LocalBroadcastManager.getInstance(this@RowerDataService)
                .sendBroadcast(Intent(RowerDataBroadcastReceiver.INTENT_KEY).apply {
                    putExtra(
                        RowerDataBroadcastReceiver.EXTRA_TIMER_DATA,
                        TimerData(
                            elapsedTimeStopwatch.elapsedSeconds.toInt(),
                            workoutStatus!!.calories,
                            workoutStatus!!.distance,
                            workoutStatus!!.rowsCount,
                            workoutStatus!!.currentRPM,
                            workoutStatus!!.currentSecsFor500M
                        )
                    )
                })
        }
    }

    inner class LocalBinder : Binder() {
        fun connect(usbDevice: UsbDevice?) {
            usbHidConnectionManager.connect(usbDevice)
        }
    }

    private suspend fun createNewWorkout() {
        val nowTime = Date()
        val wName = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ITALY).format(nowTime)
        workoutRepository.insertWorkout(Workout(null, wName, nowTime))
    }

    private fun retryConnection() {
        connectionRetryHandler = Handler(Looper.getMainLooper()).also {
            it.postDelayed({
                usbHidConnectionManager.connect(null)
            }, resources.getInteger(R.integer.connection_retry_timeout_ms).toLong())
        }
    }

    private fun sendConnectionStatus(status: Boolean) {
        ioScope.launch {
            usbServiceRepository.emitEvent(UsbServiceConnectionStatusEvent(status))
        }
    }

    override fun onUsbConnected(port: UsbSerialPort, connection: UsbDeviceConnection) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
            if (usbProtocol != null) {
                usbProtocol!!.stop()
            }

            usbProtocol =
                UsbSerialProtocol(this@RowerDataService, port, connection, this@RowerDataService)
            try {
                usbProtocol!!.start()
                createNewWorkout()
                sendConnectionStatus(true)
            } catch (exc: UsbDeviceProtocol.UsbProtocolException) {
                onUsbConnectionError(exc)
            }
            /*
            rowerSerialMcu = RowerSerialMcu(
                applicationContext,
                port,
                connection,
                this@RowerDataService
            )
            rowerSerialMcu!!.start()*/
        }
    }

    override fun onUsbConnectionError(e: Exception?) {
        e?.printStackTrace()
        usbProtocol?.stop()
        sendConnectionStatus(false)
        retryConnection()
    }

    override fun onUsbPermissionError(device: UsbDevice) {
        ioScope.launch {
            usbServiceRepository.emitEvent(UsbServicePermissionRequiredEvent(device))
        }
    }
}