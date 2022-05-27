package it.paoloinfante.rowerplus.views.parameters

import android.widget.TextView
import it.paoloinfante.rowerplus.models.TimerData

abstract class ViewParameter(private val viewAttached: TextView) {
    abstract fun getStatusProperty(status: TimerData?, bpmValue: Int?): String

    fun update(status: TimerData?, bpmValue: Int?) {
        viewAttached.text = getStatusProperty(status, bpmValue)
    }

    val currentValue: String
        get() = viewAttached.text.toString()
}