package it.paoloinfante.rowerplus.views.parameters

import android.content.Context
import android.widget.TextView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData

class TimeFor500Parameter(
    private val context: Context,
    viewAttached: TextView,
    titleView: TextView
) : ViewParameter(viewAttached) {
    init {
        titleView.text = context.getString(R.string.parameters_view_time_for_500m)
    }

    override fun getStatusProperty(status: TimerData?, bpmValue: Int?): String {
        return context.getString(
            R.string.time_for_500m_format,
            status!!.currentSecsFor500M / 60,
            status.currentSecsFor500M % 60
        )
    }
}