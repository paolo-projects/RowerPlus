package it.paoloinfante.rowerplus.models.events
import android.hardware.usb.UsbDevice

class UsbServicePermissionRequiredEvent(val device: UsbDevice): UsbServiceEvent()