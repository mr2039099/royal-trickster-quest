package com.royaltrickster.quest.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.royaltrickster.quest.R
import com.royaltrickster.quest.databinding.ViewMaskBadgeBinding
import com.royaltrickster.quest.domain.model.Mask

class MaskBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewMaskBadgeBinding

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val padH = resources.getDimensionPixelSize(R.dimen.space_sm)
        val padV = resources.getDimensionPixelSize(R.dimen.space_xxs)
        setPadding(padH, padV, padH, padV)
        setBackgroundResource(R.drawable.clay_pill_cream)
        binding = ViewMaskBadgeBinding.inflate(LayoutInflater.from(context), this)
    }

    fun bind(mask: Mask, compact: Boolean) {
        binding.badgeIcon.setImageResource(iconFor(mask.id))
        binding.badgeLabel.text = mask.name
        binding.badgeLabel.visibility = if (compact) GONE else VISIBLE
        contentDescription = mask.name + " " + mask.tagline
        ViewCompat.setStateDescription(this, mask.ability.label)
    }

    companion object {
        fun iconFor(maskId: String): Int = when (maskId) {
            "harlequin" -> R.drawable.sprite_mask_shard
            "gilded_crown" -> R.drawable.sprite_crown_coin
            "phantom" -> R.drawable.sprite_trickster_hero
            else -> R.drawable.sprite_trickster_mask
        }
    }
}
