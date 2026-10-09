package com.royaltrickster.quest.domain.model

data class Quest(
    val number: Int,
    val name: String,
    val crownTarget: Int,
    val speedFactor: Float,
    val propMix: List<String>
)
