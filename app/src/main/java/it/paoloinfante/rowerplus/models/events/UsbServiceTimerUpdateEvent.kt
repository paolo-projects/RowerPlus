package it.paoloinfante.rowerplus.models.events
import it.paoloinfante.rowerplus.models.TimerData


data class UsbServiceTimerUpdateEvent(
    val timerData: TimerData
): UsbServiceEvent()