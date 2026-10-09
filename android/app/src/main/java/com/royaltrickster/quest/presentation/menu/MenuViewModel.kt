package com.royaltrickster.quest.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royaltrickster.quest.data.sample.SampleData
import com.royaltrickster.quest.domain.repository.MaskRepository
import com.royaltrickster.quest.domain.usecase.GetPlayerProgressUseCase
import com.royaltrickster.quest.domain.usecase.GetQuestListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MenuViewModel(
    private val getQuestList: GetQuestListUseCase,
    private val getPlayerProgress: GetPlayerProgressUseCase,
    private val maskRepository: MaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        MenuUiState.empty(
            SampleData.defaultProgress(),
            SampleData.maskById(SampleData.MASK_JESTER),
            SampleData.masks.size
        )
    )
    val state: StateFlow<MenuUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val progress = getPlayerProgress()
            _state.value = MenuUiState(
                quests = getQuestList(),
                progress = progress,
                equippedMask = maskRepository.maskById(progress.equippedMaskId),
                totalMasks = maskRepository.masks().size,
                loaded = true
            )
        }
    }

    fun currentQuestNumber(): Int = _state.value.progress.currentQuest
}
