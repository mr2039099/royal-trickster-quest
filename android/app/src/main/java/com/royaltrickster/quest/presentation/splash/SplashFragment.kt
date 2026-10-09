package com.royaltrickster.quest.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.royaltrickster.quest.MainActivity
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding

    private val viewModel: SplashViewModel by viewModels()
    private var animator: SplashAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = FragmentSplashBinding.inflate(inflater, container, false)
        _binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        ServiceLocator.init(view.context.applicationContext)
        val reducedMotion = readReducedMotion()
        val helper = SplashAnimator(resources.displayMetrics.density)
        animator = helper
        helper.playEntry(
            bound.splashMask,
            bound.splashDisc,
            bound.splashTitleOne,
            bound.splashTitleTwo,
            bound.splashSubtitle
        )
        helper.playIdleLoops(
            bound.splashMask,
            listOf(
                bound.confettiOne,
                bound.confettiTwo,
                bound.confettiThree,
                bound.confettiFour,
                bound.confettiFive,
                bound.confettiSix
            ),
            reducedMotion
        )
        observeHandOff()
    }

    private fun readReducedMotion(): Boolean = try {
        ServiceLocator.settingsRepository.settings().reducedMotion
    } catch (e: Exception) {
        false
    }

    private fun observeHandOff() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.handOffReady.collect { ready ->
                    if (ready) {
                        handOff()
                    }
                }
            }
        }
    }

    private fun handOff() {
        try {
            if (!isAdded || view == null || _binding == null) {
                return
            }
            val host = activity as? MainActivity ?: return
            if (!host.navigator.showMenu()) {
                return
            }
            viewModel.consumeHandOff()
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        animator?.cancelAll()
        animator = null
        _binding?.splashMask?.animate()?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
