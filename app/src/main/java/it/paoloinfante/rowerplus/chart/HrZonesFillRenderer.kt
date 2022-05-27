package it.paoloinfante.rowerplus.chart

import android.graphics.Canvas
import android.graphics.Path
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import it.paoloinfante.rowerplus.utils.HeartRateZones
import kotlin.math.roundToInt


class HrZonesFillRenderer(
    private val hrZones: HeartRateZones,
    chart: LineDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {
    override fun drawLinearFill(
        c: Canvas,
        dataSet: ILineDataSet,
        trans: Transformer,
        bounds: XBounds
    ) {
        val filled: Path = mGenerateFilledPathBuffer

        val startingIndex = bounds.min
        val endingIndex = bounds.range + bounds.min
        val indexInterval = 128

        var currentStartIndex = 0
        var currentEndIndex = indexInterval
        var iterations = 0

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.

        // Doing this iteratively in order to avoid OutOfMemory errors that can happen on large bounds sets.
        do {
            currentStartIndex = startingIndex + iterations * indexInterval
            currentEndIndex = currentStartIndex + indexInterval
            currentEndIndex = if (currentEndIndex > endingIndex) endingIndex else currentEndIndex
            if (currentStartIndex <= currentEndIndex) {
                for (n in currentStartIndex until currentEndIndex) {
                    val entry = dataSet.getEntryForIndex(n)
                    val nextEntry = dataSet.getEntryForIndex(n + 1)
                    val fillMin = dataSet.fillFormatter.getFillLinePosition(dataSet, mChart)
                    val phaseY = mAnimator.phaseY
                    filled.reset()
                    filled.moveTo(entry.x, fillMin)
                    filled.lineTo(entry.x, entry.y * phaseY)
                    filled.lineTo(nextEntry.x, nextEntry.y * phaseY)
                    filled.lineTo(nextEntry.x, fillMin)
                    filled.lineTo(entry.x, fillMin)

                    trans.pathValueToPixel(filled)

                    drawFilledPath(
                        c,
                        filled,
                        hrZones.getColorFor(entry.y.roundToInt()),
                        dataSet.fillAlpha
                    )
                }
                //generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled)
            }
            iterations++
        } while (currentStartIndex <= currentEndIndex)
    }

    private fun generateFilledPath(
        dataSet: ILineDataSet,
        startIndex: Int,
        endIndex: Int,
        outputPath: Path
    ) {
        val fillMin = dataSet.fillFormatter.getFillLinePosition(dataSet, mChart)
        val phaseY = mAnimator.phaseY
        val isDrawSteppedEnabled = dataSet.mode == LineDataSet.Mode.STEPPED
        outputPath.reset()
        val entry = dataSet.getEntryForIndex(startIndex)
        outputPath.moveTo(entry.x, fillMin)
        outputPath.lineTo(entry.x, entry.y * phaseY)

        // create a new path
        var currentEntry: Entry? = null
        var previousEntry = entry
        for (x in startIndex + 1..endIndex) {
            currentEntry = dataSet.getEntryForIndex(x)
            if (isDrawSteppedEnabled) {
                outputPath.lineTo(currentEntry.x, previousEntry.y * phaseY)
            }
            outputPath.lineTo(currentEntry.x, currentEntry.y * phaseY)
            previousEntry = currentEntry
        }

        // close up
        if (currentEntry != null) {
            outputPath.lineTo(currentEntry.x, fillMin)
        }
        outputPath.close()
    }
}