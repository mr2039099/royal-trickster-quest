package com.royaltrickster.quest.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royaltrickster.quest.core.config.GameConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _handOffReady = MutableStateFlow(false)
    val handOffReady: StateFlow<Boolean> = _handOffReady.asStateFlow()

    private val _progressLabelVisible = MutableStateFlow(true)
    val progressLabelVisible: StateFlow<Boolean> = _progressLabelVisible.asStateFlow()

    private var timer: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        if (timer != null) {
            return
        }
        timer = viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            _handOffReady.value = true
        }
    }

    fun consumeHandOff() {
        _handOffReady.value = false
        _progressLabelVisible.value = false
    }

    override fun onCleared() {
        timer?.cancel()
        timer = null
        super.onCleared()
    }
}
