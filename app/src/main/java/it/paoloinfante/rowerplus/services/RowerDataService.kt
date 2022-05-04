package it.paoloinfante.rowerplus.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.TypedValue
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import it.paoloinfante.rowerplus.database.repositories.WorkoutStatusRepository
import it.paoloinfante.rowerplus.models.RowerPull
import it.paoloinfante.rowerplus.receiver.RowerConnectionStatusBroadcastReceiver
import it.paoloinfante.rowerplus.serial.RowerSerialMcu
import it.paoloinfante.rowerplus.utils.Stopwatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.floor

@AndroidEntryPoint
class RowerDataService : Service(), RowerSerialMcu.RowerSerialDataListener {
    companion object {
        const val EXTRA_DEVICE: String = "rowerdataservice_extra_device"
        const val EXTRA_DEVICE_CONNECTION: String = "rowerdataservice_extra_device_connection"
    }

    private var ROWS_PER_CALORIE: Float = 0f
    private var METERS_PER_ROW: Float = 0f

    private lateinit var rowerSerialMcu: RowerSerialMcu
    private lateinit var workoutStatus: WorkoutStatus

    private val elapsedTimeStopwatch = Stopwatch()
    private val localBinder = LocalBinder()

    private var stopWatchPauseTask = Handler(Looper.getMainLooper())
    private var connectionRetryHandler = Handler(Looper.getMainLooper())

    private var lastRowTime: Date = Date()

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    @Inject
    lateinit var workoutStatusRepository: WorkoutStatusRepository

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()

        val rowsPerCaloriesTyped = TypedValue()
        resources.getValue(R.dimen.rows_per_calorie, rowsPerCaloriesTyped, true)
        ROWS_PER_CALORIE = rowsPerCaloriesTyped.float

        val metersPerRowTyped = TypedValue()
        resources.getValue(R.dimen.meters_per_row, metersPerRowTyped, true)
        METERS_PER_ROW = metersPerRowTyped.float
    }

    override fun onBind(p0: Intent?): IBinder {
        return localBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        connectionRetryHandler.removeCallbacksAndMessages(null)
        rowerSerialMcu.stop()

        return false
    }

    override fun onDestroy() {
    }

    override fun onDataReceived(pull: RowerPull) {
        elapsedTimeStopwatch.start()
        stopWatchPauseTask.removeCallbacks(pauseStopwatch)
        stopWatchPauseTask.postDelayed(
            pauseStopwatch,
            resources.getInteger(R.integer.rowing_timer_timeout_ms).toLong()
        )

        workoutStatus.timeElapsed = floor(elapsedTimeStopwatch.elapsedSeconds).toInt()
        workoutStatus.calories += 1f / ROWS_PER_CALORIE
        workoutStatus.distance += METERS_PER_ROW
        workoutStatus.rowsCount++

        val newRowTime = Date()
        if (workoutStatus.rowsCount > 0) {
            workoutStatus.currentRPM = 60000f / (newRowTime.time - lastRowTime.time)
            workoutStatus.currentSecsFor500M =
                (newRowTime.time - lastRowTime.time).toFloat() / 1000 * (500f / METERS_PER_ROW)
        }
        lastRowTime = newRowTime

        ioScope.launch {
            workoutStatusRepository.pushStatus(workoutStatus)
        }
    }

    override fun onError(e: Exception?) {
        sendConnectionStatus(false)

        connectionRetryHandler.postDelayed({
            retryConnection()
        }, resources.getInteger(R.integer.connection_retry_timeout_ms).toLong())
    }

    private val pauseStopwatch = Runnable { elapsedTimeStopwatch.stop() }

    inner class LocalBinder : Binder() {
        fun connect(serialPort: UsbSerialPort, serialDeviceConnection: UsbDeviceConnection) {
            CoroutineScope(Dispatchers.IO + Job()).launch {
                rowerSerialMcu = RowerSerialMcu(
                    applicationContext,
                    serialPort,
                    serialDeviceConnection,
                    this@RowerDataService
                )

                createNewWorkout()
                val workoutId = workoutRepository.getLastWorkoutId()

                if (workoutId != null) {
                    workoutStatus = WorkoutStatus(null, workoutId)
                    sendConnectionStatus(true)
                    rowerSerialMcu.start()
                } else {
                    //todo: error
                }
            }
        }
    }

    private suspend fun createNewWorkout() {
        val nowTime = Date()
        val wName = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ITALY).format(nowTime)
        workoutRepository.insert(Workout(null, wName, nowTime))
    }

    private fun retryConnection() {
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        if (availableDrivers.size > 0) {
            val serialDriver = availableDrivers[0]

            val deviceToConnectTo = serialDriver!!.device

            val usbConnection = usbManager.openDevice(deviceToConnectTo)
            if (usbConnection == null) {
                // Couldn't connect to USB device
                onError(null)
            } else {
                val serialPort = serialDriver.ports[0]
                rowerSerialMcu.stop()
                rowerSerialMcu = RowerSerialMcu(
                    applicationContext,
                    serialPort,
                    usbConnection,
                    this@RowerDataService
                )

                sendConnectionStatus(true)

                rowerSerialMcu.start()
            }
        } else {
            // No available USB devices
            onError(null)
        }
    }

    private fun sendConnectionStatus(status: Boolean) {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(RowerConnectionStatusBroadcastReceiver.INTENT_KEY).apply {
                putExtra(RowerConnectionStatusBroadcastReceiver.EXTRA_IS_CONNECTED, status)
            })
    }
}