package com.royaltrickster.quest.domain.usecase

import com.royaltrickster.quest.domain.model.Mask
import com.royaltrickster.quest.domain.repository.MaskRepository

class UnlockMasksUseCase(private val repository: MaskRepository) {

    operator fun invoke(shards: Int): List<Mask> = repository.unlockForShards(shards)
}
