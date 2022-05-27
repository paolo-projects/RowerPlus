package it.paoloinfante.rowerplus.viewmodels

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.ble.HeartRateBLEManager
import it.paoloinfante.rowerplus.repositories.SharedSettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(private val sharedSettingsRepository: SharedSettingsRepository) :
    ViewModel(), HeartRateBLEManager.Listener {
    private lateinit var bleManager: HeartRateBLEManager

    private val _bleStatus = MutableStateFlow(false)
    private val _bleMeasurements = MutableSharedFlow<Int>()

    val bleStatus = _bleStatus.asStateFlow()
    val bleMeasurements = _bleMeasurements.asSharedFlow()

    private var scanListener: ScanResultListener? = null

    interface ScanResultListener {
        fun onScanResult(result: List<ScanResult>)
        fun onPermissionsRequired()
        fun onScanStarted()
        fun onScanEnded()
    }

    fun attach(context: Context, listener: ScanResultListener) {
        scanListener = listener
        bleManager = HeartRateBLEManager(context, this)

        val defaultBleDevice = sharedSettingsRepository.getDefaultHRBleName()
        if (defaultBleDevice != null) {
            bleManager.autoConnect(defaultBleDevice)
        }
    }

    fun requestBleScan() {
        bleManager.scan()
    }

    fun requestDeviceConnect(device: BluetoothDevice) {
        bleManager.connect(device)
    }

    override fun onPermissionRequired() {
        scanListener?.onPermissionsRequired()
    }

    override fun onScanResult(result: List<ScanResult>) {
        scanListener?.onScanResult(result)
    }

    override fun onMeasurementReceived(bpm: Int) {
        viewModelScope.launch {
            _bleMeasurements.emit(bpm)
        }
    }

    override fun onConnected(device: BluetoothDevice) {
        sharedSettingsRepository.setDefaultHRBleName(device.name)
        _bleStatus.value = true
    }

    override fun onDisconnected() {
        _bleStatus.value = false
    }

    override fun onScanFailed(errorCode: Int) {
    }

    override fun onBluetoothDisabledError() {
    }

    override fun onScanStarted() {
        scanListener?.onScanStarted()
    }

    override fun onScanEnded() {
        scanListener?.onScanEnded()
    }
}