package it.paoloinfante.rowerplus.serial

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class UsbConnectionManager(private val context: Context, private val listener: Listener) {

    interface Listener {
        fun onUsbConnected(port: UsbSerialPort, connection: UsbDeviceConnection)
        fun onUsbConnectionError(e: Exception?)
        fun onUsbPermissionError(device: UsbDevice)
    }

    class UsbConnectionException(message: String) : Exception(message)
    class UsbPermissionsException(message: String) : Exception(message)

    fun connect(usbDevice: UsbDevice?) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        var deviceToConnectTo = usbDevice
        var serialDriverToConnectTo: UsbSerialDriver? = null

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        if (deviceToConnectTo == null) {
            if (availableDrivers.size > 0) {
                serialDriverToConnectTo = availableDrivers[0]
                deviceToConnectTo = serialDriverToConnectTo.device
            } else {
                // No available USB devices
                listener.onUsbConnectionError(UsbConnectionException("No USB-Serial devices found"))
                return
            }
        } else {
            serialDriverToConnectTo = availableDrivers.firstOrNull {
                it.device == deviceToConnectTo
            }
        }

        if(serialDriverToConnectTo == null) {
            listener.onUsbConnectionError(UsbConnectionException("No Serial Driver found for the device"))
        } else {
            val usbConnection = usbManager.openDevice(deviceToConnectTo)
            if (usbConnection == null) {
                // Couldn't connect to USB device
                listener.onUsbPermissionError(deviceToConnectTo!!)
            } else {
                val serialPort = serialDriverToConnectTo.ports[0]
                listener.onUsbConnected(serialPort, usbConnection)
            }
        }
    }
}