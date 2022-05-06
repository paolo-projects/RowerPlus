package it.paoloinfante.rowerplus.ui

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import kotlin.math.round

class PulseAnimation(private val startColor: Int, private val pulseColor: Int, private val durationMs: Int) {
    private val halfDuration = round(durationMs / 2f).toLong()

    fun animate(target: View) {
        val colorAnimationForward = ValueAnimator.ofObject(ArgbEvaluator(), startColor, pulseColor)
        //colorAnimationForward.duration = halfDuration
        colorAnimationForward.addUpdateListener { target.setBackgroundColor(it.animatedValue as Int) }

        val colorAnimationBackwards = ValueAnimator.ofObject(ArgbEvaluator(), pulseColor, startColor)
        //colorAnimationBackwards.duration = halfDuration
        colorAnimationBackwards.addUpdateListener { target.setBackgroundColor(it.animatedValue as Int) }

        val animator = AnimatorSet().apply {
            duration = durationMs.toLong()
            interpolator = DecelerateInterpolator()
        }
        animator.playSequentially(colorAnimationForward, colorAnimationBackwards)
        animator.start()
    }
}