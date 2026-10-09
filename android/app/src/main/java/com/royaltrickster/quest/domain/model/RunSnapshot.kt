package com.royaltrickster.quest.domain.model

data class RunSnapshot(
    val entities: List<RunEntity>,
    val lanePosition: Float,
    val jumpProgress: Float,
    val scrollPhase: Float,
    val runnerVisible: Boolean,
    val ghost: Boolean,
    val magnet: Boolean
) {
    companion object {
        val EMPTY = RunSnapshot(
            entities = emptyList(),
            lanePosition = 1f,
            jumpProgress = 0f,
            scrollPhase = 0f,
            runnerVisible = true,
            ghost = false,
            magnet = false
        )
    }
}
