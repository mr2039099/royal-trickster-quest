package com.royaltrickster.quest.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.royaltrickster.quest.MainActivity
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.core.ui.MaskBadgeView
import com.royaltrickster.quest.core.util.ViewModelFactory
import com.royaltrickster.quest.databinding.FragmentGameBinding
import com.royaltrickster.quest.domain.model.RunEvent
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding

    private val questNumber: Int
        get() = arguments?.getInt(ARG_QUEST, 1) ?: 1

    private val viewModel: GameViewModel by viewModels {
        ViewModelFactory {
            GameViewModel(
                questNumber,
                ServiceLocator.questRepository,
                ServiceLocator.maskRepository,
                ServiceLocator.settingsRepository,
                ServiceLocator.saveRunResult
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentGameBinding.inflate(inflater, container, false)
        _binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        ServiceLocator.init(view.context.applicationContext)
        bound.gameAbilityButton.setIconResource(MaskBadgeView.iconFor(ServiceLocator.maskRepository.equippedId()))
        bound.gameBoard.setBoardSizeListener { widthDp, heightDp ->
            try {
                if (!isAdded || _binding == null) {
                    return@setBoardSizeListener
                }
                viewModel.setBoardSize(widthDp, heightDp)
            } catch (e: Exception) {
                return@setBoardSizeListener
            }
        }
        bound.gameBtnBack.setOnClickListener { leaveToMenu() }
        bound.gameBtnLeft.setOnClickListener { viewModel.onLeft() }
        bound.gameBtnRight.setOnClickListener { viewModel.onRight() }
        bound.gameBtnJump.setOnClickListener { viewModel.onJump() }
        bound.gameAbilityButton.setOnClickListener { viewModel.onAbility() }
        bound.gameBoard.setOnTouchListener { target, event ->
            if (event.actionMasked == MotionEvent.ACTION_UP) {
                try {
                    viewModel.onBoardTap(bound.gameBoard.laneForTouchX(event.x))
                    target.performClick()
                } catch (e: Exception) {
                    return@setOnTouchListener true
                }
            }
            true
        }
        observeState()
        observeEffects()
        observeResult()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun observeEffects() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { event -> playEffect(event) }
            }
        }
    }

    private fun observeResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resultReady.collect { ready ->
                    if (ready) {
                        goToResult()
                    }
                }
            }
        }
    }

    private fun render(state: GameUiState) {
        val bound = binding ?: return
        bound.gameQuestLabel.text =
            getString(R.string.game_quest_label, state.questNumber, state.questName)
        bound.gameQuestProgress.progress = state.progressPercent
        bound.gameHearts.bind(state.lives, true)
        bound.gameBoard.render(state.snapshot)
        bound.gameAbilityRing.setProgress(state.abilityProgress, state.abilityActive)
        bound.gameBtnJump.isEnabled = state.jumpAvailable
        bound.gameBtnJump.alpha = if (state.jumpAvailable) 1f else 0.45f
        ViewCompat.setStateDescription(
            bound.gameBtnJump,
            getString(
                if (state.jumpAvailable) R.string.state_jump_ready else R.string.state_jump_recharging
            )
        )
        bound.gameAbilityButton.isEnabled = state.abilityReady
        bound.gameAbilityButton.alpha = if (state.abilityReady || state.abilityActive) 1f else 0.45f
        bound.gameAbilityLabel.text = when {
            state.abilityActive -> getString(R.string.game_ability_active, state.abilityLabel)
            state.abilityReady -> getString(R.string.game_ability_ready)
            else -> getString(R.string.game_ability_charging)
        }
        ViewCompat.setStateDescription(
            bound.gameAbilityButton,
            getString(
                if (state.abilityReady) R.string.game_ability_ready
                else R.string.state_ability_recharging
            )
        )
        if (state.phase == GameUiState.Phase.READY && state.countdownValue > 0) {
            bound.gameCountdown.visibility = View.VISIBLE
            bound.gameCountdown.text = state.countdownValue.toString()
            popCountdown()
        } else if (bound.gameCountdown.visibility == View.VISIBLE) {
            bound.gameCountdown.visibility = View.GONE
        }
    }

    private fun popCountdown() {
        val bound = binding ?: return
        try {
            bound.gameCountdown.animate().cancel()
            bound.gameCountdown.scaleX = 0.7f
            bound.gameCountdown.scaleY = 0.7f
            bound.gameCountdown.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(320L)
                .setInterpolator(OvershootInterpolator(2.2f))
                .start()
        } catch (e: Exception) {
            bound.gameCountdown.scaleX = 1f
        }
    }

    private fun playEffect(event: RunEvent) {
        val bound = binding ?: return
        when (event) {
            RunEvent.CROWN_COLLECTED -> floatPickup()
            RunEvent.SHARD_COLLECTED -> floatPickup()
            RunEvent.COMBO_REACHED -> showCombo()
            RunEvent.HIT_TAKEN -> flashHit()
            else -> bound.gameFlash.alpha = 0f
        }
    }

    private fun floatPickup() {
        val bound = binding ?: return
        if (viewModel.reducedMotion) {
            return
        }
        try {
            bound.gamePickupLabel.animate().cancel()
            bound.gamePickupLabel.visibility = View.VISIBLE
            bound.gamePickupLabel.alpha = 1f
            bound.gamePickupLabel.translationY = 0f
            bound.gamePickupLabel.animate()
                .translationY(-36f * resources.displayMetrics.density)
                .alpha(0f)
                .setDuration(420L)
                .withEndAction {
                    try {
                        if (!isAdded || view == null || _binding == null) {
                            return@withEndAction
                        }
                        _binding?.gamePickupLabel?.visibility = View.INVISIBLE
                    } catch (e: Exception) {
                        return@withEndAction
                    }
                }
                .start()
        } catch (e: Exception) {
            bound.gamePickupLabel.visibility = View.INVISIBLE
        }
    }

    private fun showCombo() {
        val bound = binding ?: return
        try {
            bound.gameComboRibbon.animate().cancel()
            bound.gameComboRibbon.text =
                getString(R.string.game_combo_ribbon, viewModel.state.value.combo)
            bound.gameComboRibbon.visibility = View.VISIBLE
            bound.gameComboRibbon.alpha = 1f
            bound.gameComboRibbon.animate()
                .alpha(0f)
                .setStartDelay(600L)
                .setDuration(320L)
                .withEndAction {
                    try {
                        if (!isAdded || view == null || _binding == null) {
                            return@withEndAction
                        }
                        _binding?.gameComboRibbon?.visibility = View.INVISIBLE
                    } catch (e: Exception) {
                        return@withEndAction
                    }
                }
                .start()
        } catch (e: Exception) {
            bound.gameComboRibbon.visibility = View.INVISIBLE
        }
    }

    private fun flashHit() {
        val bound = binding ?: return
        try {
            bound.gameFlash.animate().cancel()
            bound.gameFlash.alpha = 1f
            bound.gameFlash.animate()
                .alpha(0f)
                .setDuration(240L)
                .withEndAction {
                    try {
                        if (!isAdded || view == null || _binding == null) {
                            return@withEndAction
                        }
                        _binding?.gameFlash?.alpha = 0f
                    } catch (e: Exception) {
                        return@withEndAction
                    }
                }
                .start()
            if (!viewModel.reducedMotion) {
                bound.gameBoard.animate()
                    .translationX(6f * resources.displayMetrics.density)
                    .setDuration(130L)
                    .withEndAction {
                        try {
                            if (!isAdded || view == null || _binding == null) {
                                return@withEndAction
                            }
                            _binding?.gameBoard?.animate()
                                ?.translationX(0f)
                                ?.setDuration(130L)
                                ?.start()
                        } catch (e: Exception) {
                            return@withEndAction
                        }
                    }
                    .start()
            }
        } catch (e: Exception) {
            bound.gameFlash.alpha = 0f
        }
    }

    private fun goToResult() {
        try {
            if (!isAdded || view == null) {
                return
            }
            val host = activity as? MainActivity ?: return
            if (!host.navigator.showResult()) {
                return
            }
            viewModel.consumeResult()
        } catch (e: Exception) {
            return
        }
    }

    private fun leaveToMenu() {
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
        _binding?.gameBoard?.setBoardSizeListener(null)
        _binding?.gameBoard?.setOnTouchListener(null)
        _binding?.gameBoard?.animate()?.cancel()
        _binding?.gameFlash?.animate()?.cancel()
        _binding?.gameComboRibbon?.animate()?.cancel()
        _binding?.gamePickupLabel?.animate()?.cancel()
        _binding?.gameCountdown?.animate()?.cancel()
        _binding?.gameHearts?.cancelAnimations()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_QUEST = "quest_number"

        fun newInstance(questNumber: Int): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putInt(ARG_QUEST, questNumber)
            fragment.arguments = args
            return fragment
        }
    }
}
