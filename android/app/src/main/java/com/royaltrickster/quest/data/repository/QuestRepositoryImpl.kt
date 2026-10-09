package com.royaltrickster.quest.data.repository

import com.royaltrickster.quest.data.local.ProgressPreferences
import com.royaltrickster.quest.data.sample.SampleData
import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.model.Quest
import com.royaltrickster.quest.domain.model.RunResult
import com.royaltrickster.quest.domain.repository.QuestRepository

class QuestRepositoryImpl(
    private val preferences: ProgressPreferences
) : QuestRepository {

    private var cached: PlayerProgress = preferences.load()
    private var lastRun: RunResult? = null

    override fun quests(): List<Quest> = SampleData.quests

    override fun questByNumber(number: Int): Quest = SampleData.questByNumber(number)

    override fun progress(): PlayerProgress = cached

    override fun saveRun(result: RunResult): PlayerProgress {
        val nextQuest = if (result.completed) {
            (result.questNumber + 1).coerceAtMost(SampleData.quests.size)
        } else {
            cached.currentQuest
        }
        val completed = if (result.completed) {
            maxOf(cached.completedQuests, result.questNumber)
        } else {
            cached.completedQuests
        }
        val updated = cached.copy(
            crowns = cached.crowns + result.crowns,
            shards = cached.shards + result.shards,
            currentQuest = nextQuest,
            completedQuests = completed,
            bestDistanceMeters = maxOf(cached.bestDistanceMeters, result.distanceMeters),
            bestCombo = maxOf(cached.bestCombo, result.bestCombo),
            recovered = false
        )
        cached = updated
        preferences.save(updated)
        return updated
    }

    override fun lastResult(): RunResult? = lastRun

    override fun storeLastResult(result: RunResult) {
        lastRun = result
    }

    override fun reset() {
        cached = SampleData.defaultProgress()
        lastRun = null
        preferences.clear()
        preferences.save(cached)
    }

    fun applyMaskState(equippedId: String, unlocked: Set<String>) {
        cached = cached.copy(equippedMaskId = equippedId, unlockedMaskIds = unlocked)
        preferences.save(cached)
    }
}
