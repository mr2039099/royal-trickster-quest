package com.royaltrickster.quest.domain.model

enum class MaskAbility(
    val label: String,
    val description: String,
    val durationMs: Long,
    val cooldownMs: Long
) {
    NONE("NONE", "Baseline run, no ability", 0L, 0L),
    SLOW("SLOW", "Slows the parade to a stroll", 4000L, 12000L),
    MAGNET("MAGNET", "Draws every crown to you", 5000L, 14000L),
    GHOST("GHOST", "Walk straight through the props", 3000L, 16000L)
}
