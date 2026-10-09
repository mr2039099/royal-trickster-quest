package com.royaltrickster.quest.domain.repository

import com.royaltrickster.quest.domain.model.GameSettings

interface SettingsRepository {
    fun settings(): GameSettings
    fun setSoundEnabled(enabled: Boolean)
    fun setHapticsEnabled(enabled: Boolean)
    fun setReducedMotion(enabled: Boolean)
    fun setDifficulty(difficulty: String)
}
