package com.royaltrickster.quest.presentation.splash

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

class SplashAnimator(private val density: Float) {

    private val animators = ArrayList<Animator>(16)

    fun playEntry(mask: View, disc: View, titleOne: View, titleTwo: View, subtitle: View) {
        try {
            prepareHidden(mask)
            prepareHidden(disc)
            prepareShifted(titleOne)
            prepareShifted(titleTwo)
            prepareShifted(subtitle)

            val discScale = AnimatorSet()
            discScale.playTogether(
                ObjectAnimator.ofFloat(disc, View.SCALE_X, 0.72f, 1.06f, 1.0f),
                ObjectAnimator.ofFloat(disc, View.SCALE_Y, 0.72f, 1.06f, 1.0f),
                ObjectAnimator.ofFloat(disc, View.ALPHA, 0f, 1f)
            )
            discScale.duration = ENTRY_DURATION_MS
            discScale.interpolator = OvershootInterpolator(2.2f)

            val maskScale = AnimatorSet()
            maskScale.playTogether(
                ObjectAnimator.ofFloat(mask, View.SCALE_X, 0.72f, 1.06f, 1.0f),
                ObjectAnimator.ofFloat(mask, View.SCALE_Y, 0.72f, 1.06f, 1.0f),
                ObjectAnimator.ofFloat(mask, View.ALPHA, 0f, 1f)
            )
            maskScale.duration = ENTRY_DURATION_MS
            maskScale.interpolator = OvershootInterpolator(2.2f)

            track(discScale)
            track(maskScale)
            discScale.start()
            maskScale.start()

            riseIn(titleOne, TITLE_DELAY_MS)
            riseIn(titleTwo, TITLE_DELAY_MS + 120L)
            riseIn(subtitle, SUBTITLE_DELAY_MS)
        } catch (e: Exception) {
            resetImmediate(mask, disc, titleOne, titleTwo, subtitle)
        }
    }

    fun playIdleLoops(mask: View, confetti: List<View>, reducedMotion: Boolean) {
        if (reducedMotion) {
            return
        }
        try {
            val bob = ObjectAnimator.ofFloat(mask, View.TRANSLATION_Y, 0f, -9f * density, 0f)
            bob.duration = IDLE_BOB_MS
            bob.repeatCount = ValueAnimator.INFINITE
            bob.repeatMode = ValueAnimator.RESTART
            bob.interpolator = AccelerateDecelerateInterpolator()
            bob.startDelay = ENTRY_DURATION_MS
            track(bob)
            bob.start()

            confetti.forEachIndexed { index, view ->
                val drift = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, 14f * density, 0f)
                drift.duration = CONFETTI_MS
                drift.repeatCount = ValueAnimator.INFINITE
                drift.repeatMode = ValueAnimator.RESTART
                drift.interpolator = AccelerateDecelerateInterpolator()
                drift.startDelay = (index * CONFETTI_STAGGER_MS)
                track(drift)
                drift.start()

                val tilt = ObjectAnimator.ofFloat(view, View.ROTATION, -8f, 8f)
                tilt.duration = CONFETTI_MS
                tilt.repeatCount = ValueAnimator.INFINITE
                tilt.repeatMode = ValueAnimator.REVERSE
                tilt.interpolator = AccelerateDecelerateInterpolator()
                tilt.startDelay = (index * CONFETTI_STAGGER_MS)
                track(tilt)
                tilt.start()
            }
        } catch (e: Exception) {
            cancelAll()
        }
    }

    private fun riseIn(view: View, delayMs: Long) {
        val set = AnimatorSet()
        set.playTogether(
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 26f * density, 0f),
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
        )
        set.duration = TITLE_DURATION_MS
        set.startDelay = delayMs
        set.interpolator = DecelerateInterpolator()
        track(set)
        set.start()
    }

    private fun prepareHidden(view: View) {
        view.alpha = 0f
        view.scaleX = 0.72f
        view.scaleY = 0.72f
    }

    private fun prepareShifted(view: View) {
        view.alpha = 0f
        view.translationY = 26f * density
    }

    private fun resetImmediate(vararg views: View) {
        views.forEach { view ->
            try {
                view.alpha = 1f
                view.scaleX = 1f
                view.scaleY = 1f
                view.translationY = 0f
            } catch (e: Exception) {
                return
            }
        }
    }

    private fun track(animator: Animator) {
        animators.add(animator)
    }

    fun cancelAll() {
        animators.forEach { animator ->
            try {
                animator.cancel()
            } catch (e: Exception) {
                return
            }
        }
        animators.clear()
    }

    private companion object {
        const val ENTRY_DURATION_MS = 620L
        const val TITLE_DURATION_MS = 480L
        const val TITLE_DELAY_MS = 240L
        const val SUBTITLE_DELAY_MS = 420L
        const val IDLE_BOB_MS = 2200L
        const val CONFETTI_MS = 2600L
        const val CONFETTI_STAGGER_MS = 180L
    }
}
