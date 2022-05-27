package it.paoloinfante.rowerplus.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager

class UsbHidConnectionManager(private val context: Context, private val listener: Listener) {

    interface Listener {
        fun onUsbConnected(device: UsbDevice, connection: UsbDeviceConnection)
        fun onUsbConnectionError(e: Exception?)
        fun onUsbPermissionError(device: UsbDevice)
    }

    class UsbConnectionException(message: String) : Exception(message)
    class UsbPermissionsException(message: String) : Exception(message)

    fun connect(vid: Int, pid: Int) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val device = usbManager.deviceList.values.firstOrNull {
            it.vendorId == vid && it.productId == pid
        }
            ?: return listener.onUsbConnectionError(UsbConnectionException("The USB Device with the specified VID and PID has not been found"))

        connect(device)
    }

    fun connect(usbDevice: UsbDevice) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val usbConnection = usbManager.openDevice(usbDevice)
        if (usbConnection == null) {
            // Couldn't connect to USB device
            listener.onUsbPermissionError(usbDevice)
        } else {
            listener.onUsbConnected(usbDevice, usbConnection)
        }
    }
}