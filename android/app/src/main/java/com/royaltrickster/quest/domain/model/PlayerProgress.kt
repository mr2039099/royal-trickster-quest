package com.royaltrickster.quest.domain.model

data class PlayerProgress(
    val crowns: Int,
    val shards: Int,
    val currentQuest: Int,
    val completedQuests: Int,
    val equippedMaskId: String,
    val unlockedMaskIds: Set<String>,
    val bestDistanceMeters: Int,
    val bestCombo: Int,
    val recovered: Boolean
) {
    val unlockedMaskCount: Int
        get() = unlockedMaskIds.size
}
