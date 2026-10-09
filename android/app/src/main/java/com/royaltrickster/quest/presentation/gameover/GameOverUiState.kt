package com.royaltrickster.quest.presentation.gameover

import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.model.RunResult

data class GameOverUiState(
    val result: RunResult,
    val progress: PlayerProgress,
    val loaded: Boolean
) {
    val visibleStatCount: Int
        get() {
            var count = 0
            if (result.crowns > 0) count++
            if (result.distanceMeters > 0) count++
            if (result.bestCombo > 0) count++
            if (result.shards > 0) count++
            return count
        }

    val useSummaryLine: Boolean
        get() = visibleStatCount < 2
}
