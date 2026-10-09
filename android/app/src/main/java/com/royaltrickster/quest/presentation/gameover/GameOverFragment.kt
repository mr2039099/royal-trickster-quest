package com.royaltrickster.quest.presentation.gameover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.royaltrickster.quest.MainActivity
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.config.GameConfig
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.core.ui.StatCardView
import com.royaltrickster.quest.core.util.ViewModelFactory
import com.royaltrickster.quest.databinding.FragmentGameOverBinding
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding

    private val viewModel: GameOverViewModel by viewModels {
        ViewModelFactory {
            GameOverViewModel(ServiceLocator.questRepository, ServiceLocator.getPlayerProgress)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentGameOverBinding.inflate(inflater, container, false)
        _binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        ServiceLocator.init(view.context.applicationContext)
        bound.resultCtaAgain.setOnClickListener { replay() }
        bound.resultCtaMenu.setOnClickListener { backToMenu() }
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: GameOverUiState) {
        val bound = binding ?: return
        if (!state.loaded) {
            return
        }
        val result = state.result
        if (result.completed) {
            bound.resultBanner.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.secondary_gold)
            )
            bound.resultTitle.text = getString(R.string.result_title_win)
            bound.resultTitle.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_primary)
            )
            bound.resultBannerSprite.setImageResource(R.drawable.sprite_crown_coin)
            bound.resultBannerSprite.rotation = 0f
        } else {
            bound.resultBanner.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.surface_white)
            )
            bound.resultTitle.text = getString(R.string.result_title_loss)
            bound.resultTitle.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.accent_red)
            )
            bound.resultBannerSprite.setImageResource(R.drawable.sprite_trickster_mask)
            bound.resultBannerSprite.rotation = -12f
        }
        bound.resultHearts.bind(if (result.completed) GameConfig.MAX_LIVES else 0, false)
        renderStats(state)
        renderUnlock(state)
        playBannerEntry(result.completed)
    }

    private fun renderStats(state: GameOverUiState) {
        val bound = binding ?: return
        val result = state.result
        if (state.useSummaryLine) {
            bound.resultStatRowOne.visibility = View.INVISIBLE
            bound.resultStatRowTwo.visibility = View.GONE
            bound.resultSummary.visibility = View.VISIBLE
            bound.resultSummary.text = getString(
                R.string.result_summary_line,
                result.crowns,
                getString(R.string.result_distance_format, result.distanceMeters.toString())
            )
            return
        }
        bound.resultSummary.visibility = View.GONE
        bound.resultStatRowOne.visibility = View.VISIBLE
        bound.resultStatRowTwo.visibility = View.VISIBLE
        bound.resultStatCrowns.bindOrHide(
            getString(R.string.result_stat_crowns),
            result.crowns,
            R.color.secondary_gold
        )
        bound.resultStatDistance.bindOrHide(
            getString(R.string.result_stat_distance),
            result.distanceMeters,
            getString(
                R.string.result_distance_format,
                StatCardView.formatNumber(result.distanceMeters)
            ),
            R.color.primary_purple
        )
        bound.resultStatCombo.bindOrHide(
            getString(R.string.result_stat_combo),
            result.bestCombo,
            getString(R.string.result_combo_format, result.bestCombo),
            R.color.accent_red
        )
        bound.resultStatShards.bindOrHide(
            getString(R.string.result_stat_shards),
            result.shards,
            R.color.primary_velvet
        )
    }

    private fun renderUnlock(state: GameOverUiState) {
        val bound = binding ?: return
        val name = state.result.unlockedMaskName
        if (name.isNullOrEmpty()) {
            bound.resultUnlockCard.visibility = View.GONE
            return
        }
        bound.resultUnlockCard.visibility = View.VISIBLE
        bound.resultUnlockText.text = getString(R.string.result_new_mask, name.uppercase())
        try {
            bound.resultUnlockCard.translationX = 120f * resources.displayMetrics.density
            bound.resultUnlockCard.animate()
                .translationX(0f)
                .setDuration(340L)
                .start()
        } catch (e: Exception) {
            bound.resultUnlockCard.translationX = 0f
        }
    }

    private fun playBannerEntry(win: Boolean) {
        val bound = binding ?: return
        try {
            bound.resultBanner.animate().cancel()
            bound.resultBanner.scaleX = 0.84f
            bound.resultBanner.scaleY = 0.84f
            bound.resultBanner.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(520L)
                .setInterpolator(OvershootInterpolator(2.4f))
                .start()
        } catch (e: Exception) {
            bound.resultBanner.scaleX = 1f
        }
        if (win) {
            dropConfetti()
        }
    }

    private fun dropConfetti() {
        val bound = binding ?: return
        try {
            val layer = bound.resultConfettiLayer
            layer.removeAllViews()
            val density = resources.displayMetrics.density
            val random = Random(System.currentTimeMillis())
            val size = (14 * density).toInt()
            for (index in 0 until CONFETTI_COUNT) {
                val piece = ImageView(layer.context)
                piece.setImageResource(R.drawable.ic_confetti_diamond)
                piece.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                piece.imageTintList = ContextCompat.getColorStateList(
                    layer.context,
                    if (index % 2 == 0) R.color.secondary_gold else R.color.text_on_dark
                )
                val params = FrameLayout.LayoutParams(size, size)
                params.leftMargin = (random.nextInt(300) * density).toInt()
                piece.layoutParams = params
                piece.translationY = -40f * density
                layer.addView(piece)
                piece.animate()
                    .translationY(560f * density)
                    .rotationBy(360f)
                    .alpha(0f)
                    .setStartDelay((index * 90L))
                    .setDuration(2200L)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction {
                        try {
                            if (!isAdded || view == null || _binding == null) {
                                return@withEndAction
                            }
                            _binding?.resultConfettiLayer?.removeView(piece)
                        } catch (e: Exception) {
                            return@withEndAction
                        }
                    }
                    .start()
            }
        } catch (e: Exception) {
            bound.resultConfettiLayer.removeAllViews()
        }
    }

    private fun replay() {
        try {
            if (!isAdded) {
                return
            }
            val host = activity as? MainActivity ?: return
            host.navigator.restartGame(viewModel.replayQuestNumber())
        } catch (e: Exception) {
            return
        }
    }

    private fun backToMenu() {
        try {
            if (!isAdded) {
                return
            }
            val host = activity as? MainActivity ?: return
            host.navigator.backToMenu()
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        val layer = _binding?.resultConfettiLayer
        if (layer != null) {
            for (index in 0 until layer.childCount) {
                try {
                    layer.getChildAt(index).animate().cancel()
                } catch (e: Exception) {
                    layer.getChildAt(index).clearAnimation()
                }
            }
            layer.removeAllViews()
        }
        _binding?.resultBanner?.animate()?.cancel()
        _binding?.resultUnlockCard?.animate()?.cancel()
        _binding?.resultHearts?.cancelAnimations()
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val CONFETTI_COUNT = 14
    }
}
