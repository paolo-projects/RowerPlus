package it.paoloinfante.rowerplus.utils

import java.util.*

class Stopwatch(startImmediately: Boolean = false) {
    private val elapsedTime: Long
        get() = intervals.fold(0) { acc, timeSpan ->
            acc + (timeSpan.end?.time ?: Date().time) - timeSpan.start.time
        }

    val elapsedMilliseconds: Long
        get() = elapsedTime

    val elapsedSeconds: Float
        get() = elapsedTime / 1000f

    val elapsedMinutes: Float
        get() = elapsedTime / (60 * 1000f)

    val elapsedHours: Float
        get() = elapsedTime / (60 * 60 * 1000f)

    val elapsedDays: Float
        get() = elapsedTime / (24 * 60 * 60 * 1000f)

    private val intervals = ArrayList<TimeSpan>()

    init {
        if(startImmediately) {
            start()
        }
    }

    /**
     * Starts (or resumes) the stopwatch
     */
    fun start() {
        if(intervals.size == 0 || intervals.last().end != null) {
            intervals.add(TimeSpan(Date(), null))
        }
    }

    /**
     * Stops (or pauses) the stopwatch
     */
    fun stop() {
        if(intervals.size > 0 && intervals.last().end == null) {
            intervals.last().end = Date()
        }
    }

    /**
     * Resets the stopwatch, stopping it if previously started
     */
    fun reset() {
        intervals.clear()
    }
}