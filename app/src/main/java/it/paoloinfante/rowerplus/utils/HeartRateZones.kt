package it.paoloinfante.rowerplus.utils

import android.content.Context
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import it.paoloinfante.rowerplus.R
import java.util.*
import kotlin.math.roundToInt

class HeartRateZones(context: Context, private val colors: Map<Zones, Int>, userAge: Int) {
    enum class Zones {
        LOW_INTENSITY,
        ZONE_1,
        ZONE_2,
        ZONE_3,
        ZONE_4,
        ZONE_5,
        CRITICAL;
    }

    private val zonesDescriptions = mapOf(
        Zones.LOW_INTENSITY to context.getString(R.string.hr_zones_low_intensity),
        Zones.ZONE_1 to context.getString(R.string.hr_zones_zone_1),
        Zones.ZONE_2 to context.getString(R.string.hr_zones_zone_2),
        Zones.ZONE_3 to context.getString(R.string.hr_zones_zone_3),
        Zones.ZONE_4 to context.getString(R.string.hr_zones_zone_4),
        Zones.ZONE_5 to context.getString(R.string.hr_zones_zone_5),
        Zones.CRITICAL to context.getString(R.string.hr_zones_critical),
    )

    private val maxBpm = 220 - userAge
    private val zones = mapOf(
        Zones.LOW_INTENSITY to (0 to (0.5 * maxBpm).roundToInt()),
        Zones.ZONE_1 to ((0.5 * maxBpm).roundToInt() to (0.6 * maxBpm).roundToInt()),
        Zones.ZONE_2 to ((0.6 * maxBpm).roundToInt() to (0.7 * maxBpm).roundToInt()),
        Zones.ZONE_3 to ((0.7 * maxBpm).roundToInt() to (0.8 * maxBpm).roundToInt()),
        Zones.ZONE_4 to ((0.8 * maxBpm).roundToInt() to (0.9 * maxBpm).roundToInt()),
        Zones.ZONE_5 to ((0.9 * maxBpm).roundToInt() to maxBpm),
        Zones.CRITICAL to (maxBpm to Int.MAX_VALUE)
    )

    fun getZone(bpm: Int): Zones =
        zones.entries.first { zone -> bpm >= zone.value.first && bpm < zone.value.second }.key

    fun getBpmRange(zone: Zones): Pair<Int, Int> = zones[zone]!!

    fun getColorFor(bpm: Int): Int {
        return colors[getZone(bpm)]!!
    }

    fun getPieData(bpmValues: List<Entry>): List<PieEntry> =
        bpmValues.fold(EnumMap<Zones, Int>(Zones::class.java)) { pieData, value ->
            val zone = getZone(value.y.roundToInt())
            pieData[zone] = (pieData[zone] ?: 0) + value.y.roundToInt()
            pieData
        }.entries.map { PieEntry(it.value.toFloat(), zonesDescriptions[it.key]) }
}