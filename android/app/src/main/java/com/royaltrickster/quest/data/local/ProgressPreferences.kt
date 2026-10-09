package com.royaltrickster.quest.data.local

import android.content.Context
import android.content.SharedPreferences
import com.royaltrickster.quest.data.sample.SampleData
import com.royaltrickster.quest.domain.model.PlayerProgress

class ProgressPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun load(): PlayerProgress {
        val fallback = SampleData.defaultProgress()
        return try {
            val maskIds = prefs.getStringSet(KEY_UNLOCKED, null)
                ?.filter { id -> SampleData.masks.any { it.id == id } }
                ?.toSet()
                .orEmpty()
            val resolvedMasks = if (maskIds.isEmpty()) fallback.unlockedMaskIds else maskIds
            val equipped = prefs.getString(KEY_EQUIPPED, SampleData.MASK_JESTER)
                ?: SampleData.MASK_JESTER
            val safeEquipped = if (resolvedMasks.contains(equipped)) equipped else SampleData.MASK_JESTER
            val quest = prefs.getInt(KEY_CURRENT_QUEST, 1).coerceIn(1, SampleData.quests.size)
            PlayerProgress(
                crowns = prefs.getInt(KEY_CROWNS, 0).coerceAtLeast(0),
                shards = prefs.getInt(KEY_SHARDS, 0).coerceAtLeast(0),
                currentQuest = quest,
                completedQuests = prefs.getInt(KEY_COMPLETED, 0).coerceIn(0, SampleData.quests.size),
                equippedMaskId = safeEquipped,
                unlockedMaskIds = resolvedMasks,
                bestDistanceMeters = prefs.getInt(KEY_BEST_DISTANCE, 0).coerceAtLeast(0),
                bestCombo = prefs.getInt(KEY_BEST_COMBO, 0).coerceAtLeast(0),
                recovered = false
            )
        } catch (e: Exception) {
            fallback.copy(recovered = true)
        }
    }

    fun save(progress: PlayerProgress) {
        try {
            prefs.edit()
                .putInt(KEY_CROWNS, progress.crowns)
                .putInt(KEY_SHARDS, progress.shards)
                .putInt(KEY_CURRENT_QUEST, progress.currentQuest)
                .putInt(KEY_COMPLETED, progress.completedQuests)
                .putString(KEY_EQUIPPED, progress.equippedMaskId)
                .putStringSet(KEY_UNLOCKED, progress.unlockedMaskIds)
                .putInt(KEY_BEST_DISTANCE, progress.bestDistanceMeters)
                .putInt(KEY_BEST_COMBO, progress.bestCombo)
                .apply()
        } catch (e: Exception) {
            prefs.edit().clear().apply()
        }
    }

    fun clear() {
        try {
            prefs.edit().clear().apply()
        } catch (e: Exception) {
            prefs.edit().apply()
        }
    }

    private companion object {
        const val FILE_NAME = "royal_trickster_prefs"
        const val KEY_CROWNS = "crowns"
        const val KEY_SHARDS = "shards"
        const val KEY_CURRENT_QUEST = "current_quest"
        const val KEY_COMPLETED = "completed_quests"
        const val KEY_EQUIPPED = "equipped_mask"
        const val KEY_UNLOCKED = "unlocked_masks"
        const val KEY_BEST_DISTANCE = "best_distance"
        const val KEY_BEST_COMBO = "best_combo"
    }
}
