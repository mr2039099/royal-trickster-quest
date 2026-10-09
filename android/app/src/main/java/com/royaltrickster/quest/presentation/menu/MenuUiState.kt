package com.royaltrickster.quest.presentation.menu

import com.royaltrickster.quest.domain.model.Mask
import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.model.Quest

data class MenuUiState(
    val quests: List<Quest>,
    val progress: PlayerProgress,
    val equippedMask: Mask,
    val totalMasks: Int,
    val loaded: Boolean
) {
    val showStats: Boolean
        get() = progress.crowns > 0 && progress.unlockedMaskCount > 0 && progress.completedQuests > 0

    companion object {
        fun empty(fallbackProgress: PlayerProgress, fallbackMask: Mask, totalMasks: Int) = MenuUiState(
            quests = emptyList(),
            progress = fallbackProgress,
            equippedMask = fallbackMask,
            totalMasks = totalMasks,
            loaded = false
        )
    }
}
