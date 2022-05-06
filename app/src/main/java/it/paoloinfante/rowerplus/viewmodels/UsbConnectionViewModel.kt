package it.paoloinfante.rowerplus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.models.events.UsbServiceConnectionStatusEvent
import it.paoloinfante.rowerplus.models.events.UsbServicePermissionRequiredEvent
import it.paoloinfante.rowerplus.models.events.UsbServiceTimerUpdateEvent
import it.paoloinfante.rowerplus.repositories.UsbServiceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsbConnectionViewModel @Inject constructor(private val usbServiceRepository: UsbServiceRepository): ViewModel() {
    private val _connectionStatusEvents = MutableSharedFlow<UsbServiceConnectionStatusEvent>()
    private val _permissionRequiredEvents = MutableSharedFlow<UsbServicePermissionRequiredEvent>()
    private val _timerDataEvents = MutableSharedFlow<UsbServiceTimerUpdateEvent>()

    val connectionStatusEvents: SharedFlow<UsbServiceConnectionStatusEvent> = _connectionStatusEvents.asSharedFlow()
    val permissionRequiredEvents: SharedFlow<UsbServicePermissionRequiredEvent> = _permissionRequiredEvents.asSharedFlow()
    val timerDataEvents: SharedFlow<UsbServiceTimerUpdateEvent> = _timerDataEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            usbServiceRepository.usbServiceEvents.collect {
                when (it) {
                    is UsbServiceConnectionStatusEvent -> {
                        _connectionStatusEvents.emit(it)
                    }
                    is UsbServicePermissionRequiredEvent -> {
                        _permissionRequiredEvents.emit(it)
                    }
                    is UsbServiceTimerUpdateEvent -> {
                        _timerDataEvents.emit(it)
                    }
                }
            }
        }
    }
}