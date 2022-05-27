package it.paoloinfante.rowerplus.views.parameters

import android.content.Context
import android.widget.TextView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData

class CaloriesParameter(private val context: Context, viewAttached: TextView, titleView: TextView) :
    ViewParameter(viewAttached) {
    init {
        titleView.text = context.getString(R.string.parameters_view_calories)
    }

    override fun getStatusProperty(status: TimerData?, bpmValue: Int?): String {
        return context.getString(R.string.calories_format, status!!.calories)
    }
}