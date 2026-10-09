package com.royaltrickster.quest.core.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.royaltrickster.quest.R
import com.royaltrickster.quest.databinding.ViewQuestChipBinding

class QuestChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    enum class State { LOCKED, CURRENT, COMPLETED }

    private val binding: ViewQuestChipBinding
    private var pulse: ObjectAnimator? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val pad = resources.getDimensionPixelSize(R.dimen.space_xs)
        setPadding(pad, pad, pad, pad)
        binding = ViewQuestChipBinding.inflate(LayoutInflater.from(context), this)
    }

    fun bind(number: Int, name: String, state: State, animate: Boolean) {
        binding.chipNumber.text = context.getString(R.string.chip_quest_number, number)
        binding.chipName.text = name
        stopPulse()
        when (state) {
            State.LOCKED -> applyLocked()
            State.CURRENT -> applyCurrent(animate)
            State.COMPLETED -> applyCompleted()
        }
        contentDescription = name + " " + state.name.lowercase()
        ViewCompat.setStateDescription(this, state.name.lowercase())
    }

    private fun applyLocked() {
        setBackgroundResource(R.drawable.chip_quest_locked)
        val muted = ContextCompat.getColor(context, R.color.text_muted)
        binding.chipNumber.setTextColor(muted)
        binding.chipName.setTextColor(muted)
        binding.chipStateIcon.visibility = View.VISIBLE
        binding.chipStateIcon.setImageResource(R.drawable.ic_lock)
        binding.chipStateIcon.imageTintList = ColorStateList.valueOf(muted)
        alpha = 0.75f
        isEnabled = false
    }

    private fun applyCurrent(animate: Boolean) {
        setBackgroundResource(R.drawable.chip_quest_current)
        val cream = ContextCompat.getColor(context, R.color.text_on_dark)
        binding.chipNumber.setTextColor(cream)
        binding.chipName.setTextColor(cream)
        binding.chipStateIcon.visibility = View.VISIBLE
        binding.chipStateIcon.setImageResource(R.drawable.ic_star)
        binding.chipStateIcon.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.secondary_gold)
        )
        alpha = 1f
        isEnabled = true
        if (animate) {
            startPulse()
        }
    }

    private fun applyCompleted() {
        setBackgroundResource(R.drawable.chip_quest_done)
        val ink = ContextCompat.getColor(context, R.color.text_primary)
        binding.chipNumber.setTextColor(ink)
        binding.chipName.setTextColor(ink)
        binding.chipStateIcon.visibility = View.VISIBLE
        binding.chipStateIcon.setImageResource(R.drawable.ic_check)
        binding.chipStateIcon.imageTintList = ColorStateList.valueOf(ink)
        alpha = 1f
        isEnabled = true
    }

    private fun startPulse() {
        val animator = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.0f, 1.03f)
        animator.duration = 1600L
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { value ->
            try {
                val scale = value.animatedValue as Float
                scaleY = scale
            } catch (e: Exception) {
                animator.cancel()
            }
        }
        animator.start()
        pulse = animator
    }

    fun stopPulse() {
        try {
            pulse?.cancel()
        } catch (e: Exception) {
            pulse = null
        }
        pulse = null
        scaleX = 1f
        scaleY = 1f
    }

    override fun onDetachedFromWindow() {
        stopPulse()
        super.onDetachedFromWindow()
    }
}
