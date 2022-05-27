package it.paoloinfante.rowerplus.views.parameters

import android.content.Context
import android.widget.TextView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData

class DistanceParameter(private val context: Context, viewAttached: TextView, titleView: TextView) :
    ViewParameter(viewAttached) {
    init {
        titleView.text = context.getString(R.string.parameters_view_distance)
    }

    override fun getStatusProperty(status: TimerData?, bpmValue: Int?): String {
        return context.getString(R.string.distance_format, status!!.distance)
    }
}