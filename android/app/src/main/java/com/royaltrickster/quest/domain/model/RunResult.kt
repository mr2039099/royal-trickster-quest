package com.royaltrickster.quest.domain.model

data class RunResult(
    val questNumber: Int,
    val questName: String,
    val completed: Boolean,
    val crowns: Int,
    val shards: Int,
    val distanceMeters: Int,
    val bestCombo: Int,
    val unlockedMaskName: String?
)
