package it.paoloinfante.rowerplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbPermissionBroadcastReceiver(private val permissionResultListener: OnPermissionResult): BroadcastReceiver() {
    interface OnPermissionResult {
        fun permissionGranted(device: UsbDevice)
        fun permissionRejected()
        fun permissionError()
    }

    companion object {
        const val ACTION_USB_PERMISSION = "it.paoloinfante.rowerplus.USB_PERMISSION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(ACTION_USB_PERMISSION == intent.action) {
            synchronized(this) {
                val device = intent.getParcelableExtra<UsbDevice?>(UsbManager.EXTRA_DEVICE)

                if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if(device != null) {
                        permissionResultListener.permissionGranted(device)
                    } else {
                        permissionResultListener.permissionError()
                    }
                } else {
                    permissionResultListener.permissionRejected()
                }
            }
        }
    }
}