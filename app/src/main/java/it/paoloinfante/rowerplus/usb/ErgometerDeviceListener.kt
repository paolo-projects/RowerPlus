package it.paoloinfante.rowerplus.usb

import it.paoloinfante.rowerplus.models.RowerPull

interface ErgometerDeviceListener {
    fun onDeviceDataReceived(pull: RowerPull)
    fun onDeviceReadError(e: Exception?)
}