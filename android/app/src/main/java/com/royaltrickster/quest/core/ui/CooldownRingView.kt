package com.royaltrickster.quest.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.royaltrickster.quest.R

class CooldownRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = RectF()
    private var progress = 1f
    private var active = false

    init {
        val stroke = resources.getDimension(R.dimen.cooldown_ring_stroke)
        trackPaint.style = Paint.Style.STROKE
        trackPaint.strokeWidth = stroke
        trackPaint.color = ContextCompat.getColor(context, R.color.stroke_cream_soft)
        sweepPaint.style = Paint.Style.STROKE
        sweepPaint.strokeWidth = stroke
        sweepPaint.strokeCap = Paint.Cap.ROUND
        sweepPaint.color = ContextCompat.getColor(context, R.color.secondary_gold)
    }

    fun setProgress(value: Float, abilityActive: Boolean) {
        val clamped = value.coerceIn(0f, 1f)
        if (clamped == progress && abilityActive == active) {
            return
        }
        progress = clamped
        active = abilityActive
        sweepPaint.color = ContextCompat.getColor(
            context,
            if (abilityActive) R.color.accent_red else R.color.secondary_gold
        )
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = trackPaint.strokeWidth
        bounds.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bounds.isEmpty) {
            return
        }
        canvas.drawArc(bounds, 0f, 360f, false, trackPaint)
        canvas.drawArc(bounds, -90f, 360f * progress, false, sweepPaint)
    }
}
