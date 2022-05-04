package it.paoloinfante.rowerplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RowerConnectionStatusBroadcastReceiver(private val connectionStatusListener: ConnectionStatusListener): BroadcastReceiver() {
    companion object {
        const val INTENT_KEY = "rower_usb_connection_status_change"
        const val EXTRA_IS_CONNECTED = "is_usb_connected"
    }

    interface ConnectionStatusListener {
        fun onConnected();
        fun onDisconnected();
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = intent.getBooleanExtra(EXTRA_IS_CONNECTED, false)
        if(isConnected) {
            connectionStatusListener.onConnected()
        } else {
            connectionStatusListener.onDisconnected()
        }
    }
}