package it.paoloinfante.rowerplus.views.parameters

import android.content.Context
import android.widget.TextView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData

class TotalStrokesParameter(
    private val context: Context,
    viewAttached: TextView,
    titleView: TextView
) : ViewParameter(viewAttached) {
    init {
        titleView.text = context.getString(R.string.parameters_view_total_strokes)
    }

    override fun getStatusProperty(status: TimerData?, bpmValue: Int?): String {
        return context.getString(R.string.total_strokes_format, status!!.strokesCount)
    }
}