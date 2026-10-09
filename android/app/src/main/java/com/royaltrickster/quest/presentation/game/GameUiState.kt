package com.royaltrickster.quest.presentation.game

import com.royaltrickster.quest.core.config.GameConfig
import com.royaltrickster.quest.domain.model.RunSnapshot

data class GameUiState(
    val phase: Phase,
    val questNumber: Int,
    val questName: String,
    val crowns: Int,
    val crownTarget: Int,
    val shards: Int,
    val combo: Int,
    val lives: Int,
    val countdownValue: Int,
    val abilityProgress: Float,
    val abilityActive: Boolean,
    val abilityReady: Boolean,
    val abilityLabel: String,
    val jumpAvailable: Boolean,
    val snapshot: RunSnapshot
) {
    enum class Phase { READY, RUNNING, HIT, COMPLETE, FAILED }

    val progressPercent: Int
        get() = if (crownTarget <= 0) 0 else ((crowns * 100) / crownTarget).coerceIn(0, 100)

    companion object {
        fun initial(questNumber: Int, questName: String, crownTarget: Int, abilityLabel: String) =
            GameUiState(
                phase = Phase.READY,
                questNumber = questNumber,
                questName = questName,
                crowns = 0,
                crownTarget = crownTarget,
                shards = 0,
                combo = 0,
                lives = GameConfig.MAX_LIVES,
                countdownValue = GameConfig.COUNTDOWN_STEPS,
                abilityProgress = 1f,
                abilityActive = false,
                abilityReady = false,
                abilityLabel = abilityLabel,
                jumpAvailable = true,
                snapshot = RunSnapshot.EMPTY
            )
    }
}
