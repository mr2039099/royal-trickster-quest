package com.royaltrickster.quest.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.royaltrickster.quest.core.config.GameConfig
import com.royaltrickster.quest.domain.model.RunEvent
import com.royaltrickster.quest.domain.model.RunResult
import com.royaltrickster.quest.domain.repository.MaskRepository
import com.royaltrickster.quest.domain.repository.QuestRepository
import com.royaltrickster.quest.domain.repository.SettingsRepository
import com.royaltrickster.quest.domain.usecase.SaveRunResultUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    questNumber: Int,
    private val questRepository: QuestRepository,
    maskRepository: MaskRepository,
    settingsRepository: SettingsRepository,
    private val saveRunResult: SaveRunResultUseCase
) : ViewModel() {

    private val quest = questRepository.questByNumber(questNumber)
    private val settings = settingsRepository.settings()
    private val mask = maskRepository.maskById(maskRepository.equippedId())
    private val engine = RunEngine(
        quest = quest,
        difficultyMultiplier = settings.speedMultiplier,
        ability = mask.ability,
        random = Random(System.currentTimeMillis())
    )

    private val _state = MutableStateFlow(
        GameUiState.initial(quest.number, quest.name, quest.crownTarget, mask.ability.label)
    )
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<RunEvent>(extraBufferCapacity = 16)
    val effects: SharedFlow<RunEvent> = _effects.asSharedFlow()

    private val _resultReady = MutableStateFlow(false)
    val resultReady: StateFlow<Boolean> = _resultReady.asStateFlow()

    val reducedMotion: Boolean = settings.reducedMotion

    private var loop: Job? = null
    private var boardReady = false
    private var saved = false

    fun setBoardSize(widthDp: Float, heightDp: Float) {
        engine.configureBoard(widthDp, heightDp)
        if (!boardReady && engine.isBoardReady()) {
            boardReady = true
            startLoop()
        }
    }

    private fun startLoop() {
        if (loop != null) {
            return
        }
        loop = viewModelScope.launch {
            runCountdown()
            runFrames()
        }
    }

    private suspend fun runCountdown() {
        for (step in GameConfig.COUNTDOWN_STEPS downTo 1) {
            _state.value = _state.value.copy(
                phase = GameUiState.Phase.READY,
                countdownValue = step
            )
            delay(GameConfig.COUNTDOWN_STEP_MS)
        }
        _state.value = _state.value.copy(
            phase = GameUiState.Phase.RUNNING,
            countdownValue = 0
        )
    }

    private suspend fun runFrames() {
        var previous = System.currentTimeMillis()
        while (!engine.finished) {
            delay(GameConfig.FRAME_INTERVAL_MS)
            val now = System.currentTimeMillis()
            val delta = (now - previous).coerceIn(1L, 48L).toFloat()
            previous = now
            val events = engine.update(delta)
            events.forEach { event -> _effects.tryEmit(event) }
            publish(events)
        }
        finish()
    }

    private fun publish(events: List<RunEvent>) {
        val hit = events.contains(RunEvent.HIT_TAKEN)
        val phase = when {
            engine.finished && engine.completed -> GameUiState.Phase.COMPLETE
            engine.finished -> GameUiState.Phase.FAILED
            hit -> GameUiState.Phase.HIT
            else -> GameUiState.Phase.RUNNING
        }
        _state.value = _state.value.copy(
            phase = phase,
            crowns = engine.crowns,
            shards = engine.shards,
            combo = engine.combo,
            lives = engine.lives,
            abilityProgress = engine.abilityProgress,
            abilityActive = engine.abilityActive,
            abilityReady = engine.abilityReady,
            jumpAvailable = !engine.isAirborne(),
            snapshot = engine.snapshot()
        )
    }

    private suspend fun finish() {
        if (saved) {
            return
        }
        saved = true
        val result = RunResult(
            questNumber = quest.number,
            questName = quest.name,
            completed = engine.completed,
            crowns = engine.crowns,
            shards = engine.shards,
            distanceMeters = engine.distanceMeters,
            bestCombo = engine.bestCombo,
            unlockedMaskName = null
        )
        saveRunResult(result)
        delay(GameConfig.RESULT_DELAY_MS)
        _resultReady.value = true
    }

    fun consumeResult() {
        _resultReady.value = false
    }

    fun onLeft() {
        engine.moveLeft()
    }

    fun onRight() {
        engine.moveRight()
    }

    fun onJump() {
        engine.jump()
    }

    fun onAbility(): Boolean = engine.triggerAbility()

    fun onBoardTap(lane: Int) {
        when (lane) {
            0 -> engine.moveLeft()
            GameConfig.LANE_COUNT - 1 -> engine.moveRight()
            else -> engine.jump()
        }
    }

    fun questNumber(): Int = quest.number

    override fun onCleared() {
        loop?.cancel()
        loop = null
        super.onCleared()
    }
}
