package it.paoloinfante.rowerplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import it.paoloinfante.rowerplus.models.TimerData

class RowerDataBroadcastReceiver(private val dataReceivedListener: DataReceivedListener) :
    BroadcastReceiver() {
    companion object {
        const val INTENT_KEY = "send_workout_timer_data"
        const val EXTRA_TIMER_DATA = "timer_data"
    }

    interface DataReceivedListener {
        fun onDataReceived(data: TimerData)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.getParcelableExtra<TimerData>(EXTRA_TIMER_DATA)
        if (data != null) {
            dataReceivedListener.onDataReceived(data)
        }
    }
}