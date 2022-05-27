package it.paoloinfante.rowerplus.ble

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import it.paoloinfante.rowerplus.R
import java.util.*

class HeartRateBLEManager(private val mContext: Context, private val listener: Listener) {
    companion object {
        private const val TAG = "HeartRateBLEManager"

        private fun uuidFromInt(uuid: Int): UUID {
            val MSB = 0x0000000000001000L
            val LSB = -0x7fffff7fa064cb05L
            val value: Long = uuid.toLong() and -0x1
            return UUID(MSB or (value shl 32), LSB)
        }

        val HR_SERVICE_UUID = uuidFromInt(0x180D)
        val HR_MEASUREMENT_CHAR_UUID = uuidFromInt(0x2A37)
        val HR_CONTROL_POINT_CHAR_UUID = uuidFromInt(0x2A39)
        val CLIENT_CHARACTERISTIC_CONFIG_UUID = uuidFromInt(0x2902)

        val PERMISSIONS = if (Build.VERSION.SDK_INT > 30)
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        else
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
    }

    private var scanTime: Int = 10000
    private var bleScanner: BluetoothLeScanner? = null
    private var connectedDevice: BluetoothDevice? = null
    private var autoConnectScanResult: BleAutoConnectScanResult? = null

    interface Listener {
        fun onPermissionRequired()
        fun onScanResult(result: List<ScanResult>)
        fun onMeasurementReceived(bpm: Int)
        fun onConnected(device: BluetoothDevice)
        fun onDisconnected()
        fun onScanFailed(errorCode: Int)
        fun onBluetoothDisabledError()
        fun onScanStarted()
        fun onScanEnded()
    }

    init {
        scanTime = mContext.resources.getInteger(R.integer.ble_scan_time)
    }

    fun autoConnect(deviceName: String) {
        autoConnectScanResult = BleAutoConnectScanResult(deviceName)
        val bluetoothService =
            mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothService.adapter

        if (!PERMISSIONS.all {
                ActivityCompat.checkSelfPermission(
                    mContext,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            listener.onPermissionRequired()
        } else {
            bleScanner = bluetoothAdapter.bluetoothLeScanner
            if (bleScanner != null) {
                val filter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(HR_SERVICE_UUID))
                    .setDeviceName(deviceName).build()

                autoConnectScanResult!!.start(
                    listOf(filter),
                    ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build()
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    bleScanner!!.stopScan(bleScanResult)
                }, scanTime.toLong())
            } else {
                listener.onBluetoothDisabledError()
            }
        }
    }

    fun scan() {
        val bluetoothService =
            mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothService.adapter

        if (!PERMISSIONS.all {
                ActivityCompat.checkSelfPermission(
                    mContext,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            listener.onPermissionRequired()
        } else {
            bleScanner = bluetoothAdapter.bluetoothLeScanner
            if (bleScanner != null) {
                autoConnectScanResult?.stop()

                val filter = ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(HR_SERVICE_UUID))
                    .build()

                bleScanner!!.startScan(
                    listOf(filter),
                    ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                    bleScanResult
                )
                listener.onScanStarted()
                Handler(Looper.getMainLooper()).postDelayed({
                    bleScanner!!.stopScan(bleScanResult)
                    listener.onScanEnded()
                }, scanTime.toLong())
            } else {
                listener.onBluetoothDisabledError()
            }
        }
    }

    fun connect(device: BluetoothDevice) {
        connectedDevice = device
        device.connectGatt(mContext, true, bleGattCallback)
    }

    private val bleGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                listener.onConnected(gatt.device)
                gatt.discoverServices()
            } else {
                listener.onDisconnected()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            val characteristic = gatt.getService(HR_SERVICE_UUID)
                .getCharacteristic(HR_MEASUREMENT_CHAR_UUID)
            gatt.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            gatt.writeDescriptor(descriptor)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            val services = gatt.services

            val service = gatt.getService(HR_SERVICE_UUID)
            val allcharacteristics = service.characteristics

            val characteristic = service
                .getCharacteristic(HR_CONTROL_POINT_CHAR_UUID)

            if (characteristic != null) {
                characteristic.value = byteArrayOf(1)
                gatt.writeCharacteristic(characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            val bpm = characteristic.value[1].toInt() and 0xFF
            Log.d(TAG, "onCharacteristicChanged: Received BPM $bpm")
            listener.onMeasurementReceived(bpm)
        }
    }

    private val bleScanResult = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            listener.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            listener.onScanResult(listOf(result))
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)

            listener.onScanResult(results.toList())
        }
    }

    inner class BleAutoConnectScanResult(private val deviceName: String) : ScanCallback() {
        private var onGoingScan = false
        private var stopScanHandler: Handler? = null

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            onGoingScan = false

            listener.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (result.device.name == deviceName) {
                connect(result.device)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)

            val matchingResult = results.firstOrNull { r -> r.device.name == deviceName }

            if (matchingResult != null) {
                connect(matchingResult.device)
            }
        }

        fun stop() {
            stopScanHandler?.removeCallbacks(stopScanCallback)
            stopScanHandler = null

            bleScanner?.stopScan(this)
            onGoingScan = false
        }

        fun start(filters: List<ScanFilter>, settings: ScanSettings) {
            bleScanner?.startScan(filters, settings, this)
            onGoingScan = true

            stopScanHandler = Handler(Looper.getMainLooper())
            stopScanHandler?.postDelayed(stopScanCallback, scanTime.toLong())
        }

        private val stopScanCallback = {
            stopScanHandler = null
            stop()
        }
    }
}