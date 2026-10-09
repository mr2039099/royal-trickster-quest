package com.royaltrickster.quest.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royaltrickster.quest.data.sample.SampleData
import com.royaltrickster.quest.domain.model.RunResult
import com.royaltrickster.quest.domain.repository.QuestRepository
import com.royaltrickster.quest.domain.usecase.GetPlayerProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameOverViewModel(
    private val questRepository: QuestRepository,
    private val getPlayerProgress: GetPlayerProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        GameOverUiState(fallbackResult(), SampleData.defaultProgress(), false)
    )
    val state: StateFlow<GameOverUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = questRepository.lastResult() ?: fallbackResult()
            _state.value = GameOverUiState(stored, getPlayerProgress(), true)
        }
    }

    fun replayQuestNumber(): Int = _state.value.result.questNumber

    private companion object {
        fun fallbackResult(): RunResult {
            val quest = SampleData.questByNumber(1)
            return RunResult(
                questNumber = quest.number,
                questName = quest.name,
                completed = false,
                crowns = 0,
                shards = 0,
                distanceMeters = 0,
                bestCombo = 0,
                unlockedMaskName = null
            )
        }
    }
}
