package it.paoloinfante.rowerplus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.paoloinfante.rowerplus.database.repositories.WorkoutRepository
import it.paoloinfante.rowerplus.models.events.UsbServiceConnectionStatusEvent
import it.paoloinfante.rowerplus.models.events.UsbServicePermissionRequiredEvent
import it.paoloinfante.rowerplus.models.events.UsbServiceTimerUpdateEvent
import it.paoloinfante.rowerplus.repositories.UsbServiceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UsbConnectionViewModel @Inject constructor(
    private val usbServiceRepository: UsbServiceRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val _connectionStatusEvents = MutableStateFlow(UsbServiceConnectionStatusEvent(false))
    private val _permissionRequiredEvents = MutableSharedFlow<UsbServicePermissionRequiredEvent>()
    private val _timerDataEvents = MutableSharedFlow<UsbServiceTimerUpdateEvent>()

    private var latestHrMeasurement = -1
    private var latestHrMeasurementTime = Date()

    val connectionStatusEvents: StateFlow<UsbServiceConnectionStatusEvent> =
        _connectionStatusEvents.asStateFlow()
    val permissionRequiredEvents: SharedFlow<UsbServicePermissionRequiredEvent> =
        _permissionRequiredEvents.asSharedFlow()
    val timerDataEvents: SharedFlow<UsbServiceTimerUpdateEvent> = _timerDataEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            async {
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

            async {
                usbServiceRepository.workoutStatus.collect {
                    val workoutId = workoutRepository.getLastWorkoutId()
                    if (workoutId != null) {
                        it.workoutId = workoutId

                        if (latestHrMeasurement > 0 && latestHrMeasurementTime.after(Date().apply { time -= 15000 })) {
                            it.heartRateBpm = latestHrMeasurement.toFloat()
                        }

                        workoutRepository.pushStatus(it)
                    }
                }
            }
        }
    }

    fun updateHrData(bpm: Int) {
        latestHrMeasurement = bpm
        latestHrMeasurementTime = Date()
    }
}