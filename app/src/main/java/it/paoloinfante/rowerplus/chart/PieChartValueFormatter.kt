package it.paoloinfante.rowerplus.chart

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class PieChartValueFormatter : ValueFormatter() {
    private val mFormat = DecimalFormat("0 %")

    override fun getFormattedValue(value: Float): String {
        return if (value > 0.0f) mFormat.format(value / 100) else ""
    }
}