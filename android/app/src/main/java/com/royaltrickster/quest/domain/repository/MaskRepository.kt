package com.royaltrickster.quest.domain.repository

import com.royaltrickster.quest.domain.model.Mask

interface MaskRepository {
    fun masks(): List<Mask>
    fun maskById(id: String): Mask
    fun equip(id: String)
    fun equippedId(): String
    fun unlockedIds(): Set<String>
    fun unlockForShards(shards: Int): List<Mask>
}
