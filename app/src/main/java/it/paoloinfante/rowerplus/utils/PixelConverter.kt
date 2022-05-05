package it.paoloinfante.rowerplus.utils

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.round

object PixelConverter {
    fun dpToPx(context: Context, dp: Float): Int {
        return round(dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun pxToDp(context: Context, px: Float): Int {
        return round(px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}