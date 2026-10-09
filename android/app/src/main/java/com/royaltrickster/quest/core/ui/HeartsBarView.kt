package com.royaltrickster.quest.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.royaltrickster.quest.R
import com.royaltrickster.quest.databinding.ViewHeartsBarBinding

class HeartsBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewHeartsBarBinding
    private val hearts: List<ImageView>
    private var shownLives = -1

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        binding = ViewHeartsBarBinding.inflate(LayoutInflater.from(context), this)
        hearts = listOf(binding.heartOne, binding.heartTwo, binding.heartThree)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        contentDescription = context.getString(R.string.cd_hearts)
    }

    fun bind(lives: Int, animate: Boolean) {
        val safe = lives.coerceIn(0, hearts.size)
        hearts.forEachIndexed { index, heart ->
            val alive = index < safe
            if (alive) {
                heart.visibility = View.VISIBLE
                heart.alpha = 1f
                heart.scaleX = 1f
                heart.scaleY = 1f
            } else if (animate && shownLives > safe && index == safe) {
                shrink(heart)
            } else {
                heart.visibility = View.VISIBLE
                heart.alpha = 0.22f
                heart.scaleX = 0.8f
                heart.scaleY = 0.8f
            }
        }
        shownLives = safe
        ViewCompat.setStateDescription(this, context.getString(R.string.state_lives_left, safe))
    }

    private fun shrink(heart: ImageView) {
        try {
            heart.animate().cancel()
            heart.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0.22f)
                .setDuration(220L)
                .withEndAction {
                    try {
                        if (!isAttachedToWindow) {
                            return@withEndAction
                        }
                        heart.scaleX = 0.8f
                        heart.scaleY = 0.8f
                        heart.alpha = 0.22f
                    } catch (e: Exception) {
                        return@withEndAction
                    }
                }
                .start()
        } catch (e: Exception) {
            heart.alpha = 0.22f
        }
    }

    fun cancelAnimations() {
        hearts.forEach { heart ->
            try {
                heart.animate().cancel()
            } catch (e: Exception) {
                heart.alpha = 1f
            }
        }
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }
}
