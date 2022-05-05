package it.paoloinfante.rowerplus

import android.app.ActionBar
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.IBinder
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.adapters.WorkoutDataViewsAdapter
import it.paoloinfante.rowerplus.database.models.Workout
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import it.paoloinfante.rowerplus.models.TimerData
import it.paoloinfante.rowerplus.receiver.RowerConnectionStatusBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.RowerDataBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.UsbPermissionBroadcastReceiver
import it.paoloinfante.rowerplus.services.RowerDataService
import it.paoloinfante.rowerplus.utils.DepthPageTransformer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UsbPermissionBroadcastReceiver.OnPermissionResult,
    RowerConnectionStatusBroadcastReceiver.ConnectionStatusListener, RowerDataBroadcastReceiver.DataReceivedListener {
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

        connectToDevice(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE))
    }

    private fun configureNavigationDrawer()
    {
        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
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
        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onConnected() {
        Toast.makeText(this, getString(R.string.message_connection_successful), Toast.LENGTH_SHORT).show()
    }

    override fun onDisconnected() {
        Toast.makeText(this, getString(R.string.error_device_disconnected), Toast.LENGTH_SHORT).show()
    }

    override fun onDataReceived(data: TimerData) {
        workoutDataViewViewModel.pushNewTimerData(data)
    }
}