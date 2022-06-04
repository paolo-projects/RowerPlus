package it.paoloinfante.rowerplus.views.parameters

import android.content.Context
import android.widget.TextView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData

class TimeParameter(private val context: Context, viewAttached: TextView, titleView: TextView) :
    ViewParameter(viewAttached) {
    init {
        titleView.text = context.getString(R.string.parameters_view_timer)
    }

    override fun getStatusProperty(status: TimerData?, bpmValue: Int?): String {
        return context.getString(
            R.string.timer_format,
            (status!!.timeElapsed / 60).toInt(),
            (status.timeElapsed % 60).toInt()
        )
    }
}