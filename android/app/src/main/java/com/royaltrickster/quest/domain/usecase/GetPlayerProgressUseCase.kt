package com.royaltrickster.quest.domain.usecase

import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.repository.QuestRepository

class GetPlayerProgressUseCase(private val repository: QuestRepository) {

    operator fun invoke(): PlayerProgress = repository.progress()
}
