package com.royaltrickster.quest.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.royaltrickster.quest.R
import com.royaltrickster.quest.core.di.ServiceLocator
import com.royaltrickster.quest.databinding.DialogSettingsBinding
import com.royaltrickster.quest.domain.model.GameSettings

class SettingsDialog : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_RoyalTricksterQuest_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val inflated = DialogSettingsBinding.inflate(inflater, container, false)
        _binding = inflated
        return inflated.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        ServiceLocator.init(view.context.applicationContext)
        val current = ServiceLocator.settingsRepository.settings()
        bound.settingsSwitchSound.isChecked = current.soundEnabled
        bound.settingsSwitchHaptics.isChecked = current.hapticsEnabled
        bound.settingsSwitchMotion.isChecked = current.reducedMotion
        applyStateDescriptions(current)
        selectDifficulty(current.difficulty)

        bound.settingsSwitchSound.setOnCheckedChangeListener { _, checked ->
            ServiceLocator.settingsRepository.setSoundEnabled(checked)
            applyStateDescriptions(ServiceLocator.settingsRepository.settings())
        }
        bound.settingsSwitchHaptics.setOnCheckedChangeListener { _, checked ->
            ServiceLocator.settingsRepository.setHapticsEnabled(checked)
            applyStateDescriptions(ServiceLocator.settingsRepository.settings())
        }
        bound.settingsSwitchMotion.setOnCheckedChangeListener { _, checked ->
            ServiceLocator.settingsRepository.setReducedMotion(checked)
            applyStateDescriptions(ServiceLocator.settingsRepository.settings())
        }
        bound.settingsDifficultyGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }
            val value = when (checkedId) {
                R.id.settings_difficulty_easy -> GameSettings.DIFFICULTY_EASY
                R.id.settings_difficulty_jester -> GameSettings.DIFFICULTY_JESTER
                else -> GameSettings.DIFFICULTY_ROYAL
            }
            ServiceLocator.settingsRepository.setDifficulty(value)
        }
        bound.settingsReset.setOnClickListener { confirmReset() }
        bound.settingsClose.setOnClickListener { dismissSafely() }
    }

    override fun onStart() {
        super.onStart()
        try {
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        } catch (e: Exception) {
            return
        }
    }

    private fun selectDifficulty(value: String) {
        val bound = binding ?: return
        val id = when (value) {
            GameSettings.DIFFICULTY_EASY -> R.id.settings_difficulty_easy
            GameSettings.DIFFICULTY_JESTER -> R.id.settings_difficulty_jester
            else -> R.id.settings_difficulty_royal
        }
        bound.settingsDifficultyGroup.check(id)
    }

    private fun applyStateDescriptions(settings: GameSettings) {
        val bound = binding ?: return
        ViewCompat.setStateDescription(bound.settingsSwitchSound, stateText(settings.soundEnabled))
        ViewCompat.setStateDescription(
            bound.settingsSwitchHaptics,
            stateText(settings.hapticsEnabled)
        )
        ViewCompat.setStateDescription(bound.settingsSwitchMotion, stateText(settings.reducedMotion))
    }

    private fun stateText(enabled: Boolean): String = getString(
        if (enabled) R.string.settings_state_on else R.string.settings_state_off
    )

    private fun confirmReset() {
        try {
            if (!isAdded) {
                return
            }
            MaterialAlertDialogBuilder(requireContext(), R.style.Theme_RoyalTricksterQuest_AlertDialog)
                .setTitle(R.string.settings_reset_title)
                .setPositiveButton(R.string.settings_reset_confirm) { dialogRef, _ ->
                    try {
                        ServiceLocator.questRepository.reset()
                    } catch (e: Exception) {
                        dialogRef.dismiss()
                    }
                    dialogRef.dismiss()
                }
                .setNegativeButton(R.string.settings_reset_cancel) { dialogRef, _ ->
                    dialogRef.dismiss()
                }
                .show()
        } catch (e: Exception) {
            return
        }
    }

    private fun dismissSafely() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
            return
        }
    }

    override fun onDestroyView() {
        _binding?.settingsSwitchSound?.setOnCheckedChangeListener(null)
        _binding?.settingsSwitchHaptics?.setOnCheckedChangeListener(null)
        _binding?.settingsSwitchMotion?.setOnCheckedChangeListener(null)
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
    }
}
