package com.royaltrickster.quest.data.local

import android.content.Context
import android.content.SharedPreferences
import com.royaltrickster.quest.domain.model.GameSettings

class SettingsPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun load(): GameSettings = try {
        GameSettings(
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            reducedMotion = prefs.getBoolean(KEY_MOTION, false),
            difficulty = prefs.getString(KEY_DIFFICULTY, GameSettings.DIFFICULTY_ROYAL)
                ?: GameSettings.DIFFICULTY_ROYAL
        )
    } catch (e: Exception) {
        GameSettings(true, true, false, GameSettings.DIFFICULTY_ROYAL)
    }

    fun setSound(enabled: Boolean) = put { it.putBoolean(KEY_SOUND, enabled) }

    fun setHaptics(enabled: Boolean) = put { it.putBoolean(KEY_HAPTICS, enabled) }

    fun setReducedMotion(enabled: Boolean) = put { it.putBoolean(KEY_MOTION, enabled) }

    fun setDifficulty(value: String) = put { it.putString(KEY_DIFFICULTY, value) }

    private fun put(block: (SharedPreferences.Editor) -> Unit) {
        try {
            val editor = prefs.edit()
            block(editor)
            editor.apply()
        } catch (e: Exception) {
            prefs.edit().apply()
        }
    }

    private companion object {
        const val FILE_NAME = "royal_trickster_settings"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_HAPTICS = "haptics_enabled"
        const val KEY_MOTION = "reduced_motion"
        const val KEY_DIFFICULTY = "difficulty"
    }
}
