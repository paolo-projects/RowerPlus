package it.paoloinfante.rowerplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FloatingWindowReceiver(private val listener: MessageListener): BroadcastReceiver() {
    companion object {
        const val INTENT_KEY = "floating_window_service"
        const val EXTRA_KILL_SERVICE = "kill_service"
    }

    interface MessageListener {
        fun onRequestServiceKill()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.getBooleanExtra(EXTRA_KILL_SERVICE, false)) {
            listener.onRequestServiceKill()
        }
    }
}