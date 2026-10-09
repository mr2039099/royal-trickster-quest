package com.royaltrickster.quest.domain.model

data class GameSettings(
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val reducedMotion: Boolean,
    val difficulty: String
) {
    val speedMultiplier: Float
        get() = when (difficulty) {
            DIFFICULTY_EASY -> 0.85f
            DIFFICULTY_JESTER -> 1.2f
            else -> 1.0f
        }

    companion object {
        const val DIFFICULTY_EASY = "EASY"
        const val DIFFICULTY_ROYAL = "ROYAL"
        const val DIFFICULTY_JESTER = "JESTER"
    }
}
