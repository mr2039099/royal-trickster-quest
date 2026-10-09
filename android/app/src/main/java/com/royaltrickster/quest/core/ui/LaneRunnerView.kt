package com.royaltrickster.quest.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.config.GameConfig
import com.royaltrickster.quest.domain.model.RunEntity
import com.royaltrickster.quest.domain.model.RunSnapshot
import kotlin.math.floor

class LaneRunnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun interface BoardSizeListener {
        fun onBoardMeasured(widthDp: Float, heightDp: Float)
    }

    private val density = resources.displayMetrics.density
    private val carpetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chevronPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val drapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val boardRect = RectF()
    private val spriteRect = Rect()

    private val boardFrameDp = GameConfig.BOARD_PADDING_DP + GameConfig.BOARD_STROKE_DP
    private var laneWidthPx = 0f
    private var boardWidthPx = 0f
    private var boardLeftPx = 0f
    private var boardTopPx = 0f
    private var boardHeightPx = 0f

    private var snapshot: RunSnapshot = RunSnapshot.EMPTY
    private var sizeListener: BoardSizeListener? = null

    private val runnerDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_trickster_run) }
    private val crownDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_crown_coin) }
    private val shardDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_mask_shard) }
    private val barrelDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_prop_barrel) }
    private val crateDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_prop_crate) }
    private val bannerDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_prop_banner) }
    private val pikeDrawable: Drawable? by lazy { loadDrawable(R.drawable.sprite_prop_pike) }

    init {
        carpetPaint.color = ContextCompat.getColor(context, R.color.accent_red)
        edgePaint.color = ContextCompat.getColor(context, R.color.secondary_gold)
        separatorPaint.color = ContextCompat.getColor(context, R.color.lane_separator)
        separatorPaint.strokeWidth = 2f * density
        chevronPaint.color = ContextCompat.getColor(context, R.color.secondary_gold)
        chevronPaint.alpha = 56
        drapePaint.color = ContextCompat.getColor(context, R.color.primary_velvet)
        drapePaint.alpha = 150
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = GameConfig.BOARD_STROKE_DP * density
        framePaint.color = ContextCompat.getColor(context, R.color.secondary_gold)
        shadowPaint.color = ContextCompat.getColor(context, R.color.primary_night)
        shadowPaint.alpha = 90
    }

    fun setBoardSizeListener(listener: BoardSizeListener?) {
        sizeListener = listener
    }

    fun render(value: RunSnapshot) {
        snapshot = value
        invalidate()
    }

    fun laneForTouchX(x: Float): Int {
        if (boardWidthPx <= 0f) {
            return GameConfig.START_LANE
        }
        val relative = ((x - boardLeftPx) / boardWidthPx).coerceIn(0f, 0.999f)
        return (relative * GameConfig.LANE_COUNT).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) {
            return
        }
        val gutterPx = GameConfig.BOARD_SIDE_GUTTER_DP * density
        val maxWidthPx = GameConfig.BOARD_MAX_WIDTH_DP * density
        val availablePx = minOf(w - gutterPx, maxWidthPx)
        val framePx = boardFrameDp * density
        laneWidthPx = floor((availablePx - 2f * framePx) / GameConfig.LANE_COUNT)
        boardWidthPx = laneWidthPx * GameConfig.LANE_COUNT + 2f * framePx
        boardLeftPx = (w - boardWidthPx) / 2f
        boardTopPx = 0f
        boardHeightPx = h.toFloat()
        boardRect.set(boardLeftPx, boardTopPx, boardLeftPx + boardWidthPx, boardTopPx + boardHeightPx)
        sizeListener?.onBoardMeasured(boardWidthPx / density, boardHeightPx / density)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (boardWidthPx <= 0f) {
            return
        }
        drawDrapes(canvas)
        drawBoard(canvas)
        drawEntities(canvas)
        drawRunner(canvas)
    }

    private fun drawDrapes(canvas: Canvas) {
        val drapeWidth = boardLeftPx
        if (drapeWidth <= 1f) {
            return
        }
        val step = 28f * density
        val offset = (snapshot.scrollPhase * 0.4f * density) % step
        var y = -step + offset
        while (y < height) {
            canvas.drawRect(0f, y, drapeWidth, y + step * 0.55f, drapePaint)
            canvas.drawRect(width - drapeWidth, y, width.toFloat(), y + step * 0.55f, drapePaint)
            y += step
        }
    }

    private fun drawBoard(canvas: Canvas) {
        val framePx = boardFrameDp * density
        val radius = 24f * density
        canvas.drawRoundRect(boardRect, radius, radius, shadowPaint)
        val inner = RectF(
            boardRect.left + framePx,
            boardRect.top,
            boardRect.right - framePx,
            boardRect.bottom
        )
        canvas.drawRect(inner, carpetPaint)
        val edge = 3f * density
        canvas.drawRect(inner.left - edge, inner.top, inner.left, inner.bottom, edgePaint)
        canvas.drawRect(inner.right, inner.top, inner.right + edge, inner.bottom, edgePaint)
        for (lane in 1 until GameConfig.LANE_COUNT) {
            val x = inner.left + laneWidthPx * lane
            canvas.drawLine(x, inner.top, x, inner.bottom, separatorPaint)
        }
        drawChevrons(canvas, inner)
        canvas.drawRoundRect(boardRect, radius, radius, framePaint)
    }

    private fun drawChevrons(canvas: Canvas, inner: RectF) {
        val step = 72f * density
        val offset = (snapshot.scrollPhase * density) % step
        val armHeight = 22f * density
        var y = inner.top - step + offset
        while (y < inner.bottom) {
            for (lane in 0 until GameConfig.LANE_COUNT) {
                val left = inner.left + laneWidthPx * lane + laneWidthPx * 0.22f
                val right = inner.left + laneWidthPx * (lane + 1) - laneWidthPx * 0.22f
                val centerX = (left + right) / 2f
                canvas.drawRect(left, y, centerX, y + armHeight * 0.26f, chevronPaint)
                canvas.drawRect(centerX, y + armHeight * 0.14f, right, y + armHeight * 0.4f, chevronPaint)
            }
            y += step
        }
    }

    private fun drawEntities(canvas: Canvas) {
        val framePx = boardFrameDp * density
        for (entity in snapshot.entities) {
            if (entity.consumed) {
                continue
            }
            val drawable = drawableFor(entity.kind) ?: continue
            val sizeDp = when (entity.kind) {
                RunEntity.Kind.CROWN -> GameConfig.CROWN_SIZE_DP
                RunEntity.Kind.SHARD -> GameConfig.SHARD_SIZE_DP
                else -> GameConfig.PROP_SIZE_DP
            }
            val sizePx = sizeDp * density
            val laneCenter = boardRect.left + framePx + laneWidthPx * (entity.lane + 0.5f)
            val top = entity.y * density - sizePx / 2f
            spriteRect.set(
                (laneCenter - sizePx / 2f).toInt(),
                top.toInt(),
                (laneCenter + sizePx / 2f).toInt(),
                (top + sizePx).toInt()
            )
            drawable.bounds = spriteRect
            drawable.alpha = 255
            drawable.draw(canvas)
        }
    }

    private fun drawRunner(canvas: Canvas) {
        val drawable = runnerDrawable ?: return
        if (!snapshot.runnerVisible) {
            return
        }
        val framePx = boardFrameDp * density
        val sizePx = GameConfig.RUNNER_SIZE_DP * density
        val laneCenter = boardRect.left + framePx + laneWidthPx * (snapshot.lanePosition + 0.5f)
        val baseY = boardRect.top + boardHeightPx * GameConfig.RUNNER_BOARD_FRACTION
        val lift = snapshot.jumpProgress * GameConfig.JUMP_HEIGHT_DP * density
        val scale = 1f + snapshot.jumpProgress * 0.08f
        val half = sizePx * scale / 2f
        val centerY = baseY - lift
        spriteRect.set(
            (laneCenter - half).toInt(),
            (centerY - half).toInt(),
            (laneCenter + half).toInt(),
            (centerY + half).toInt()
        )
        drawable.bounds = spriteRect
        drawable.alpha = if (snapshot.ghost) 140 else 255
        drawable.draw(canvas)
    }

    private fun drawableFor(kind: RunEntity.Kind): Drawable? = when (kind) {
        RunEntity.Kind.CROWN -> crownDrawable
        RunEntity.Kind.SHARD -> shardDrawable
        RunEntity.Kind.BARREL -> barrelDrawable
        RunEntity.Kind.CRATE -> crateDrawable
        RunEntity.Kind.BANNER -> bannerDrawable
        RunEntity.Kind.PIKE -> pikeDrawable
    }

    private fun loadDrawable(resId: Int): Drawable? = try {
        AppCompatResources.getDrawable(context, resId)?.mutate()
    } catch (e: Exception) {
        null
    }
}
