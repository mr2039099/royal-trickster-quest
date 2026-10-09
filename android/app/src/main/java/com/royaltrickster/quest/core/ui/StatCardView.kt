package com.royaltrickster.quest.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.royaltrickster.quest.R
import com.royaltrickster.quest.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewStatCardBinding

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val pad = resources.getDimensionPixelSize(R.dimen.space_sm)
        setPadding(pad, pad, pad, pad)
        setBackgroundResource(R.drawable.clay_card_white)
        binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)
    }

    fun bind(label: String, value: String, accentColorRes: Int) {
        val accent = ContextCompat.getColor(context, accentColorRes)
        binding.statValue.text = value
        binding.statValue.setTextColor(accent)
        binding.statLabel.text = label
        ViewCompat.setBackgroundTintList(binding.statAccentDot, ColorStateList.valueOf(accent))
        contentDescription = label + " " + value
        visibility = View.VISIBLE
    }

    fun bindOrHide(label: String, value: Int, accentColorRes: Int): Boolean {
        if (value <= 0) {
            visibility = View.GONE
            return false
        }
        bind(label, formatNumber(value), accentColorRes)
        return true
    }

    fun bindOrHide(label: String, value: Int, formatted: String, accentColorRes: Int): Boolean {
        if (value <= 0) {
            visibility = View.GONE
            return false
        }
        bind(label, formatted, accentColorRes)
        return true
    }

    companion object {
        fun formatNumber(value: Int): String {
            val raw = value.toString()
            if (raw.length <= 3) {
                return raw
            }
            val builder = StringBuilder()
            var counter = 0
            for (index in raw.length - 1 downTo 0) {
                builder.append(raw[index])
                counter++
                if (counter % 3 == 0 && index > 0) {
                    builder.append(',')
                }
            }
            return builder.reverse().toString()
        }
    }
}
