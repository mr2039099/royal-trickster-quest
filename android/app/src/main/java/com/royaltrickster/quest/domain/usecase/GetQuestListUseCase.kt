package com.royaltrickster.quest.domain.usecase

import com.royaltrickster.quest.domain.model.Quest
import com.royaltrickster.quest.domain.repository.QuestRepository

class GetQuestListUseCase(private val repository: QuestRepository) {

    operator fun invoke(): List<Quest> = repository.quests()

    fun current(): Quest {
        val progress = repository.progress()
        return repository.questByNumber(progress.currentQuest)
    }
}
