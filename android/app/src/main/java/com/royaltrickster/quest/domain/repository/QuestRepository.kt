package com.royaltrickster.quest.domain.repository

import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.model.Quest
import com.royaltrickster.quest.domain.model.RunResult

interface QuestRepository {
    fun quests(): List<Quest>
    fun questByNumber(number: Int): Quest
    fun progress(): PlayerProgress
    fun saveRun(result: RunResult): PlayerProgress
    fun lastResult(): RunResult?
    fun storeLastResult(result: RunResult)
    fun reset()
}
