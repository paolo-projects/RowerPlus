package it.paoloinfante.rowerplus.repositories

import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.models.events.UsbServiceEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UsbServiceRepository {
    private val _usbServiceEvents = MutableSharedFlow<UsbServiceEvent>()
    val usbServiceEvents: SharedFlow<UsbServiceEvent> = _usbServiceEvents.asSharedFlow()

    private val _workoutStatus = MutableSharedFlow<WorkoutStatus>()
    val workoutStatus = _workoutStatus.asSharedFlow()

    suspend fun emitEvent(event: UsbServiceEvent) {
        _usbServiceEvents.emit(event)
    }

    suspend fun emitWorkoutStatus(status: WorkoutStatus) {
        _workoutStatus.emit(status)
    }
}