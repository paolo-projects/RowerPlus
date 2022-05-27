package it.paoloinfante.rowerplus.utils

import android.content.Context
import android.text.format.DateUtils
import java.text.ParseException
import java.util.*

object DateDisplay {
    fun dateToTimeAgo(context: Context, date: Date): String {
        return try {
            val nowDate = Date()
            DateUtils.getRelativeTimeSpanString(
                date.time,
                nowDate.time,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        } catch (exc: ParseException) {
            "-"
        }
    }
}