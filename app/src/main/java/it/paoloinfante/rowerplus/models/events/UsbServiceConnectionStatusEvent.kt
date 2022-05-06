package it.paoloinfante.rowerplus.models.events

data class UsbServiceConnectionStatusEvent(
    val connected: Boolean
): UsbServiceEvent()