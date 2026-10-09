package com.royaltrickster.quest.data.repository

import com.royaltrickster.quest.data.sample.SampleData
import com.royaltrickster.quest.domain.model.Mask
import com.royaltrickster.quest.domain.repository.MaskRepository

class MaskRepositoryImpl(
    private val questRepository: QuestRepositoryImpl
) : MaskRepository {

    override fun masks(): List<Mask> = SampleData.masks

    override fun maskById(id: String): Mask = SampleData.maskById(id)

    override fun equip(id: String) {
        val unlocked = unlockedIds()
        if (!unlocked.contains(id)) {
            return
        }
        questRepository.applyMaskState(id, unlocked)
    }

    override fun equippedId(): String = questRepository.progress().equippedMaskId

    override fun unlockedIds(): Set<String> = questRepository.progress().unlockedMaskIds

    override fun unlockForShards(shards: Int): List<Mask> {
        val current = unlockedIds()
        val newly = SampleData.masks.filter { it.shardCost <= shards && !current.contains(it.id) }
        if (newly.isEmpty()) {
            return emptyList()
        }
        val merged = current + newly.map { it.id }
        questRepository.applyMaskState(equippedId(), merged)
        return newly
    }
}
