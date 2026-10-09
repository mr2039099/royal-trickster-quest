package com.royaltrickster.quest.domain.usecase

import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.model.RunResult
import com.royaltrickster.quest.domain.repository.QuestRepository

class SaveRunResultUseCase(
    private val repository: QuestRepository,
    private val unlockMasks: UnlockMasksUseCase
) {

    operator fun invoke(result: RunResult): PlayerProgress {
        val stored = repository.saveRun(result)
        val unlocked = unlockMasks(stored.shards)
        val enriched = result.copy(unlockedMaskName = unlocked.firstOrNull()?.name)
        repository.storeLastResult(enriched)
        return repository.progress()
    }
}
