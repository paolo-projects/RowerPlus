package it.paoloinfante.rowerplus.utils

import android.content.Context
import android.util.TypedValue


object DisplayUtils {
    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun spToPx(context: Context, sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun dpToSp(context: Context, dp: Float): Int {
        return (dpToPx(context, dp) / context.resources.displayMetrics.scaledDensity).toInt()
    }
}