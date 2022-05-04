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
import android.widget.Toast
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
import it.paoloinfante.rowerplus.receiver.RowerConnectionStatusBroadcastReceiver
import it.paoloinfante.rowerplus.receiver.UsbPermissionBroadcastReceiver
import it.paoloinfante.rowerplus.services.RowerDataService
import it.paoloinfante.rowerplus.utils.DepthPageTransformer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), UsbPermissionBroadcastReceiver.OnPermissionResult,
    RowerConnectionStatusBroadcastReceiver.ConnectionStatusListener {
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
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private val rowerConnectionStatusBroadcastReceiver =
        RowerConnectionStatusBroadcastReceiver(this)

    @Inject lateinit var workoutRepository: WorkoutRepository

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

        configureNavigationDrawer()
        configureToolbar()

        connectToDevice(intent.getParcelableExtra(UsbManager.EXTRA_DEVICE))
    }

    private fun configureNavigationDrawer()
    {
        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        NavigationUI.setupWithNavController(toolbar, navController, drawerLayout)
        NavigationUI.setupWithNavController(navigationView, navController)
    }

    private fun configureToolbar()
    {
        toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        setSupportActionBar(toolbar)
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0)
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        drawerLayout.addDrawerListener(drawerToggle)
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
            Toast.makeText(this, "No USB devices available", Toast.LENGTH_LONG).show()
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
            .setTitle("Permissions rejected")
            .setMessage("The permissions for the USB device have not been granted")
            .setPositiveButton("OK", null)
            .create().show()
    }

    override fun permissionError() {
        AlertDialog.Builder(this)
            .setTitle("Permissions error")
            .setMessage("An error occurred while gathering USB device permissions")
            .setPositiveButton("OK", null)
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

    private val navigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener {
        false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment).navController
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onConnected() {

        Toast.makeText(this, "Connection to USB device successful!", Toast.LENGTH_SHORT).show()
    }

    override fun onDisconnected() {
        Toast.makeText(this, "USB device disconnected! Retrying in a little while...", Toast.LENGTH_SHORT).show()
    }
}