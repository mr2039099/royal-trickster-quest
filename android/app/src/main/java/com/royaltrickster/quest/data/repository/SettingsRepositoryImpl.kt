package com.royaltrickster.quest.data.repository

import com.royaltrickster.quest.data.local.SettingsPreferences
import com.royaltrickster.quest.domain.model.GameSettings
import com.royaltrickster.quest.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val preferences: SettingsPreferences
) : SettingsRepository {

    override fun settings(): GameSettings = preferences.load()

    override fun setSoundEnabled(enabled: Boolean) = preferences.setSound(enabled)

    override fun setHapticsEnabled(enabled: Boolean) = preferences.setHaptics(enabled)

    override fun setReducedMotion(enabled: Boolean) = preferences.setReducedMotion(enabled)

    override fun setDifficulty(difficulty: String) = preferences.setDifficulty(difficulty)
}
