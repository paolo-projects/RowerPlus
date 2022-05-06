package it.paoloinfante.rowerplus.ui

import android.graphics.PointF
import android.view.animation.Interpolator

class CubicBezierInterpolator(private val p1: PointF, private val p2: PointF): Interpolator {
    companion object {
        private val p0 = PointF(0f, 0f)
        private val p3 = PointF(1f, 1f)
    }

    private fun pointOnCurve(t: Float): PointF {
        val ax: Float
        val bx: Float
        val cx: Float


        val ay: Float
        val by: Float
        val cy: Float

        val result = PointF()

        cx = 3f * (p1.x - p0.x)
        bx = 3f * (p2.x - p1.x) - cx
        ax = p3.x - p0.x - cx - bx

        cy = 3f * (p1.y - p0.y)
        by = 3f * (p2.y - p1.y) - cy
        ay = p3.y - p0.y - cy - by

        val tSqr = t*t
        val tCub = tSqr * t

        result.x = (ax * tCub) + (bx * tSqr) + (cx * t) + p0.x
        result.y = (ay * tCub) + (by * tSqr) + (cy* t) + p0.y

        return result
    }

    override fun getInterpolation(t: Float): Float {
        return pointOnCurve(t).y
    }
}