package it.paoloinfante.rowerplus

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import it.paoloinfante.rowerplus.receiver.FloatingWindowReceiver
import it.paoloinfante.rowerplus.receiver.RowerConnectionStatusBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.RowerDataBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.UsbPermissionBroadcastReceiver
import it.paoloinfante.rowerplus.services.FloatingWindowService
import it.paoloinfante.rowerplus.services.RowerDataService
import it.paoloinfante.rowerplus.viewmodels.UsbConnectionViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UsbPermissionBroadcastReceiver.OnPermissionResult {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var usbManager: UsbManager
    private val usbPermissionBroadcastReceiver = UsbPermissionBroadcastReceiver(this)
    private var usbDevice: UsbDevice? = null
    private var serviceBinder: RowerDataService.LocalBinder? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    private val usbConnectionViewModel by viewModels<UsbConnectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val filter = IntentFilter(UsbPermissionBroadcastReceiver.ACTION_USB_PERMISSION)
        registerReceiver(usbPermissionBroadcastReceiver, filter)

        listenToEvents()

        configureNavigationDrawerAndToolbar()
        checkOverlayDisplayPermission()
        startUsbDeviceService(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE))
    }

    private fun listenToEvents() {
        lifecycleScope.launch {
            async {
                usbConnectionViewModel.connectionStatusEvents.collect {
                    if (it.connected) {
                        onConnected()
                    } else {
                        onDisconnected()
                    }
                }
            }
            async {
                usbConnectionViewModel.permissionRequiredEvents.collect {
                    onPermissionError(it.device)
                }
            }
        }
    }

    private fun configureNavigationDrawerAndToolbar() {
        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        setSupportActionBar(toolbar)
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)
        NavigationUI.setupWithNavController(navigationView, navController)
    }

    private fun startUsbDeviceService(device: UsbDevice?) {
        usbDevice = device
        startDataCollectionService()
    }

    private fun askUsbPermissions(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(UsbPermissionBroadcastReceiver.ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun startDataCollectionService() {
        Intent(this, RowerDataService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStart() {
        super.onStart()

        killFloatingWindow()
    }

    override fun onStop() {
        super.onStop()

        startFloatingWindow()
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(connection)
    }

    private fun checkOverlayDisplayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayDisplayPermission()
        }
    }

    private fun requestOverlayDisplayPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(getString(R.string.alert_overlay_permission_title))
        builder.setMessage(getString(R.string.alert_overlay_permission_message))
        builder.setPositiveButton(
            getString(R.string.alert_overlay_permission_open_settings)
        ) { _, _ ->
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
        val dialog = builder.create()
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
        if (serviceBinder != null) {
            serviceBinder!!.connect(usbDevice)
        } else {
            startDataCollectionService()
        }
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
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder) {
            val serviceBinder = binder as RowerDataService.LocalBinder
            serviceBinder.connect(usbDevice)

            this@MainActivity.serviceBinder = serviceBinder
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            serviceBinder = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    private fun onConnected() {
        Toast.makeText(this, getString(R.string.message_connection_successful), Toast.LENGTH_SHORT)
            .show()
    }

    private fun onDisconnected() {
        Toast.makeText(this, getString(R.string.error_device_disconnected), Toast.LENGTH_SHORT)
            .show()
    }

    private fun onPermissionError(device: UsbDevice) {
        usbDevice = device
        askUsbPermissions(device)
    }
}