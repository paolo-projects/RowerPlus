package it.paoloinfante.rowerplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice

class RowerConnectionStatusBroadcastReceiver(private val connectionStatusListener: ConnectionStatusListener) :
    BroadcastReceiver() {
    companion object {
        const val INTENT_KEY = "rower_usb_connection_status_change"
        const val EXTRA_IS_CONNECTED = "is_usb_connected"
        const val EXTRA_PERMISSION_ERROR = "permission_error"
        const val EXTRA_PERMISSION_DEVICE = "permission_lacking_device"
    }

    interface ConnectionStatusListener {
        fun onConnected()
        fun onDisconnected()
        fun onPermissionError(device: UsbDevice)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_PERMISSION_ERROR, false)) {
            val device = intent.getParcelableExtra<UsbDevice>(
                EXTRA_PERMISSION_DEVICE
            )
            if (device != null) {
                connectionStatusListener.onPermissionError(device)
            }
        } else {
            val isConnected = intent.getBooleanExtra(EXTRA_IS_CONNECTED, false)
            if (isConnected) {
                connectionStatusListener.onConnected()
            } else {
                connectionStatusListener.onDisconnected()
            }
        }
    }
}