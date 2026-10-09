package com.royaltrickster.quest.domain.model

data class Mask(
    val id: String,
    val name: String,
    val tagline: String,
    val shardCost: Int,
    val ability: MaskAbility
)
