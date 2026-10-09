package com.royaltrickster.quest.presentation.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.royaltrickster.quest.MainActivity
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.core.ui.QuestChipView
import com.royaltrickster.quest.core.ui.StatCardView
import com.royaltrickster.quest.core.util.ViewModelFactory
import com.royaltrickster.quest.databinding.FragmentMenuBinding
import com.royaltrickster.quest.presentation.dialog.MaskWardrobeDialog
import com.royaltrickster.quest.presentation.dialog.SettingsDialog
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding

    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory {
            MenuViewModel(
                ServiceLocator.getQuestList,
                ServiceLocator.getPlayerProgress,
                ServiceLocator.maskRepository
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentMenuBinding.inflate(inflater, container, false)
        _binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        ServiceLocator.init(view.context.applicationContext)
        bound.menuCtaPlay.setOnClickListener { startQuest() }
        bound.menuBtnMasks.setOnClickListener { openWardrobe() }
        bound.menuBtnSettings.setOnClickListener { openSettings() }
        playEntry()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: MenuUiState) {
        val bound = binding ?: return
        if (!state.loaded) {
            return
        }
        bound.menuMaskBadge.bind(state.equippedMask, false)
        bound.menuCrownTotal.text = StatCardView.formatNumber(state.progress.crowns)
        bound.menuChipContainer.removeAllViews()
        val chipWidth = resources.getDimensionPixelSize(R.dimen.menu_chip_width)
        val chipGap = resources.getDimensionPixelSize(R.dimen.space_xs)
        state.quests.forEach { quest ->
            val chip = QuestChipView(bound.menuChipContainer.context)
            val params = LinearLayout.LayoutParams(chipWidth, LinearLayout.LayoutParams.MATCH_PARENT)
            params.marginEnd = chipGap
            chip.layoutParams = params
            val chipState = when {
                quest.number <= state.progress.completedQuests -> QuestChipView.State.COMPLETED
                quest.number == state.progress.currentQuest -> QuestChipView.State.CURRENT
                else -> QuestChipView.State.LOCKED
            }
            chip.bind(quest.number, quest.name, chipState, chipState == QuestChipView.State.CURRENT)
            bound.menuChipContainer.addView(chip)
        }
        if (state.showStats) {
            bound.menuStatRow.visibility = View.VISIBLE
            bound.menuFirstRunHint.visibility = View.GONE
            bound.menuStatCrowns.bindOrHide(
                getString(R.string.menu_stat_crowns),
                state.progress.crowns,
                R.color.secondary_gold
            )
            bound.menuStatMasks.bindOrHide(
                getString(R.string.menu_stat_masks),
                state.progress.unlockedMaskCount,
                state.progress.unlockedMaskCount.toString() + "/" + state.totalMasks,
                R.color.primary_purple
            )
        } else {
            bound.menuStatRow.visibility = View.INVISIBLE
            bound.menuFirstRunHint.visibility = View.VISIBLE
            bound.menuFirstRunHint.text = if (state.progress.recovered) {
                getString(R.string.menu_progress_reset)
            } else {
                getString(R.string.menu_hint_first_run)
            }
        }
    }

    private fun playEntry() {
        val bound = binding ?: return
        try {
            bound.menuSheet.translationY = 48f * resources.displayMetrics.density
            bound.menuSheet.alpha = 0f
            bound.menuSheet.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(380L)
                .setInterpolator(DecelerateInterpolator())
                .start()
            bound.menuCtaPlay.scaleX = 0.9f
            bound.menuCtaPlay.scaleY = 0.9f
            bound.menuCtaPlay.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(240L)
                .setDuration(420L)
                .setInterpolator(OvershootInterpolator(1.8f))
                .start()
            bound.menuHero.translationY = 0f
            bound.menuHero.animate()
                .translationY(-10f * resources.displayMetrics.density)
                .setDuration(1200L)
                .withEndAction {
                    try {
                        if (!isAdded || view == null || _binding == null) {
                            return@withEndAction
                        }
                        _binding?.menuHero?.animate()
                            ?.translationY(0f)
                            ?.setDuration(1200L)
                            ?.start()
                    } catch (e: Exception) {
                        return@withEndAction
                    }
                }
                .start()
        } catch (e: Exception) {
            bound.menuSheet.alpha = 1f
        }
    }

    private fun startQuest() {
        try {
            if (!isAdded) {
                return
            }
            val host = activity as? MainActivity ?: return
            host.navigator.showGame(viewModel.currentQuestNumber())
        } catch (e: Exception) {
            return
        }
    }

    private fun openWardrobe() {
        try {
            if (!isAdded || parentFragmentManager.isStateSaved) {
                return
            }
            MaskWardrobeDialog().show(parentFragmentManager, MaskWardrobeDialog.TAG)
        } catch (e: Exception) {
            return
        }
    }

    private fun openSettings() {
        try {
            if (!isAdded || parentFragmentManager.isStateSaved) {
                return
            }
            SettingsDialog().show(parentFragmentManager, SettingsDialog.TAG)
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        _binding?.menuSheet?.animate()?.cancel()
        _binding?.menuCtaPlay?.animate()?.cancel()
        _binding?.menuHero?.animate()?.cancel()
        val container = _binding?.menuChipContainer
        if (container != null) {
            for (index in 0 until container.childCount) {
                (container.getChildAt(index) as? QuestChipView)?.stopPulse()
            }
        }
        _binding = null
        super.onDestroyView()
    }
}
