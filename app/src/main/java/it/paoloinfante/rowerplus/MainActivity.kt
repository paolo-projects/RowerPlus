package it.paoloinfante.rowerplus

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.navigation.NavigationView
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import it.paoloinfante.rowerplus.models.TimerData
import it.paoloinfante.rowerplus.receiver.FloatingWindowReceiver
import it.paoloinfante.rowerplus.receiver.RowerConnectionStatusBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.RowerDataBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.UsbPermissionBroadcastReceiver
import it.paoloinfante.rowerplus.services.FloatingWindowService
import it.paoloinfante.rowerplus.services.RowerDataService


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UsbPermissionBroadcastReceiver.OnPermissionResult,
    RowerConnectionStatusBroadcastReceiver.ConnectionStatusListener,
    RowerDataBroadcastReceiver.DataReceivedListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var usbManager: UsbManager
    private val usbPermissionBroadcastReceiver = UsbPermissionBroadcastReceiver(this)
    private var serialDriver: UsbSerialDriver? = null
    private var serialPort: UsbSerialPort? = null
    private var serialDeviceConnection: UsbDeviceConnection? = null
    private var serviceBinder: RowerDataService.LocalBinder? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private val rowerDataBroadcastReceiver = RowerDataBroadcastReceiver(this)

    private val workoutDataViewViewModel by viewModels<WorkoutDataViewViewModel>()

    private val rowerConnectionStatusBroadcastReceiver =
        RowerConnectionStatusBroadcastReceiver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)

        val filter = IntentFilter(UsbPermissionBroadcastReceiver.ACTION_USB_PERMISSION)
        registerReceiver(usbPermissionBroadcastReceiver, filter)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            rowerConnectionStatusBroadcastReceiver,
            IntentFilter(RowerConnectionStatusBroadcastReceiver.INTENT_KEY)
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(
            rowerDataBroadcastReceiver,
            IntentFilter(RowerDataBroadcastReceiver.INTENT_KEY)
        )

        configureNavigationDrawer()

        checkOverlayDisplayPermission()

        connectToDevice(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE))
    }

    private fun configureNavigationDrawer() {
        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        setSupportActionBar(toolbar)
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)
        NavigationUI.setupWithNavController(navigationView, navController)
    }

    private fun connectToDevice(device: UsbDevice?) {
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        if (availableDrivers.size > 0) {
            serialDriver = availableDrivers[0]

            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(UsbPermissionBroadcastReceiver.ACTION_USB_PERMISSION),
                PendingIntent.FLAG_IMMUTABLE
            )

            val deviceToConnectTo = device ?: serialDriver!!.device

            val usbConnection = usbManager.openDevice(deviceToConnectTo)
            if (usbConnection == null) {
                usbManager.requestPermission(deviceToConnectTo, permissionIntent)
            } else if (serialDriver != null) {
                serialPort = serialDriver!!.ports[0]
                serialDeviceConnection = usbConnection
                startDataCollectionService()
            }
        } else {
            Toast.makeText(this, getString(R.string.error_no_usb_devices), Toast.LENGTH_LONG).show()
        }
    }

    private fun startDataCollectionService() {
        Intent(this, RowerDataService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()

        killFloatingWindow()
    }

    override fun onPause() {
        super.onPause()

        startFloatingWindow()
    }

    private fun checkOverlayDisplayPermission() {
        if(!Settings.canDrawOverlays(this)) {
            requestOverlayDisplayPermission()
        }
    }

    private fun requestOverlayDisplayPermission() {
        // An AlertDialog is created
        val builder = AlertDialog.Builder(this)

        // This dialog can be closed, just by
        // taping outside the dialog-box
        builder.setCancelable(true)

        // The title of the Dialog-box is set
        builder.setTitle(getString(R.string.alert_overlay_permission_title))

        // The message of the Dialog-box is set
        builder.setMessage(getString(R.string.alert_overlay_permission_message))

        // The event of the Positive-Button is set
        builder.setPositiveButton(
            getString(R.string.alert_overlay_permission_open_settings)
        ) { _, _ -> // The app will redirect to the 'Display over other apps' in Settings.
            // This is an Implicit Intent. This is needed when any Action is needed
            // to perform, here it is
            // redirecting to an other app(Settings).
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))

            // This method will start the intent. It takes two parameter,
            // one is the Intent and the other is
            // an requestCode Integer. Here it is -1.
            startActivity(intent)
        }
        val dialog = builder.create()
        // The Dialog will show in the screen
        dialog.show()
    }

    private fun killFloatingWindow() {
        LocalBroadcastManager.getInstance(this)
            .sendBroadcast(Intent(FloatingWindowReceiver.INTENT_KEY).apply {
                putExtra(FloatingWindowReceiver.EXTRA_KILL_SERVICE, true)
            })
    }

    private fun startFloatingWindow() {
        startService(Intent(this, FloatingWindowService::class.java))
    }

    override fun permissionGranted(device: UsbDevice) {
        connectToDevice(device)
    }

    override fun permissionRejected() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_permission_rejected_title))
            .setMessage(getString(R.string.error_permission_rejected_message))
            .setPositiveButton(getString(R.string.message_ok), null)
            .create().show()
    }

    override fun permissionError() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_permission_error_title))
            .setMessage(getString(R.string.error_permission_error_message))
            .setPositiveButton(getString(R.string.message_ok), null)
            .create().show()
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val serviceBinder = p1 as RowerDataService.LocalBinder
            if (serialPort != null && serialDeviceConnection != null) {
                serviceBinder.connect(serialPort!!, serialDeviceConnection!!)
            }

            this@MainActivity.serviceBinder = serviceBinder
        }

        override fun onServiceDisconnected(p0: ComponentName?) {

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onConnected() {
        Toast.makeText(this, getString(R.string.message_connection_successful), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDisconnected() {
        Toast.makeText(this, getString(R.string.error_device_disconnected), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDataReceived(data: TimerData) {
        workoutDataViewViewModel.pushNewTimerData(data)
    }
}