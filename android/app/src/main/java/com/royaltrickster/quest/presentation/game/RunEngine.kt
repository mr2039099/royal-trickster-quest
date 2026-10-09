package com.royaltrickster.quest.presentation.game

import com.royaltrickster.quest.core.config.GameConfig
import com.royaltrickster.quest.domain.model.MaskAbility
import com.royaltrickster.quest.domain.model.Quest
import com.royaltrickster.quest.domain.model.RunEntity
import com.royaltrickster.quest.domain.model.RunEvent
import com.royaltrickster.quest.domain.model.RunSnapshot
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class RunEngine(
    private val quest: Quest,
    private val difficultyMultiplier: Float,
    private val ability: MaskAbility,
    random: Random
) {

    private val spawner = PropSpawner(quest, random)
    private val entities = ArrayList<RunEntity>(48)

    private var boardWidthDp = 0f
    private var boardHeightDp = 0f

    private var elapsedMs = 0f
    private var rampAccumulatorMs = 0f
    private var speedFactor = 1f
    private var scrollPhase = 0f

    private var lanePosition = GameConfig.START_LANE.toFloat()
    private var targetLane = GameConfig.START_LANE
    private var laneAnimMs = 0f
    private var laneStart = GameConfig.START_LANE.toFloat()

    private var jumpMs = -1f
    private var invulnerableMs = 0f
    private var abilityActiveMs = 0f
    private var abilityCooldownMs = 0f

    var lives = GameConfig.MAX_LIVES
        private set
    var crowns = 0
        private set
    var shards = 0
        private set
    var combo = 0
        private set
    var bestCombo = 0
        private set
    var distanceDp = 0f
        private set
    var finished = false
        private set
    var completed = false
        private set

    val distanceMeters: Int
        get() = (distanceDp * GameConfig.METERS_PER_DP).toInt()

    val abilityProgress: Float
        get() = when {
            ability == MaskAbility.NONE -> 1f
            abilityActiveMs > 0f -> abilityActiveMs / ability.durationMs.toFloat()
            abilityCooldownMs > 0f -> 1f - (abilityCooldownMs / ability.cooldownMs.toFloat())
            else -> 1f
        }

    val abilityActive: Boolean
        get() = abilityActiveMs > 0f

    val abilityReady: Boolean
        get() = ability != MaskAbility.NONE && abilityActiveMs <= 0f && abilityCooldownMs <= 0f

    fun configureBoard(widthDp: Float, heightDp: Float) {
        boardWidthDp = widthDp
        boardHeightDp = heightDp
    }

    fun isBoardReady(): Boolean = boardWidthDp > 0f && boardHeightDp > 0f

    fun moveLeft() {
        changeLane(targetLane - 1)
    }

    fun moveRight() {
        changeLane(targetLane + 1)
    }

    fun jump() {
        if (finished || jumpMs >= 0f) {
            return
        }
        jumpMs = 0f
    }

    fun isAirborne(): Boolean = jumpMs >= 0f

    fun triggerAbility(): Boolean {
        if (!abilityReady || finished) {
            return false
        }
        abilityActiveMs = ability.durationMs.toFloat()
        return true
    }

    private fun changeLane(requested: Int) {
        if (finished) {
            return
        }
        val clamped = requested.coerceIn(0, GameConfig.LANE_COUNT - 1)
        if (clamped == targetLane) {
            return
        }
        laneStart = lanePosition
        targetLane = clamped
        laneAnimMs = 0f
    }

    fun update(deltaMs: Float): List<RunEvent> {
        if (finished || !isBoardReady()) {
            return emptyList()
        }
        val events = ArrayList<RunEvent>(4)
        elapsedMs += deltaMs
        advanceSpeed(deltaMs)
        advanceLane(deltaMs)
        advanceJump(deltaMs)
        advanceAbility(deltaMs)
        if (invulnerableMs > 0f) {
            invulnerableMs -= deltaMs
        }

        val speed = currentSpeedDpPerMs()
        val travelled = speed * deltaMs
        distanceDp += travelled
        scrollPhase = (scrollPhase + travelled) % SCROLL_PERIOD_DP

        entities.addAll(spawner.advance(deltaMs, -GameConfig.PROP_SIZE_DP))

        val runnerY = boardHeightDp * GameConfig.RUNNER_BOARD_FRACTION
        val iterator = entities.listIterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            val moved = entity.copy(y = entity.y + travelled)
            if (moved.y > boardHeightDp + GameConfig.PROP_SIZE_DP) {
                iterator.remove()
                continue
            }
            val resolved = resolve(moved, runnerY, events)
            iterator.set(resolved)
        }

        if (crowns >= quest.crownTarget) {
            finished = true
            completed = true
            events.add(RunEvent.QUEST_COMPLETE)
        } else if (lives <= 0) {
            finished = true
            completed = false
            events.add(RunEvent.QUEST_FAILED)
        } else if (elapsedMs >= GameConfig.ROUND_CAP_MS) {
            finished = true
            completed = false
            events.add(RunEvent.QUEST_FAILED)
        }
        return events
    }

    private fun resolve(entity: RunEntity, runnerY: Float, events: MutableList<RunEvent>): RunEntity {
        if (entity.consumed) {
            return entity
        }
        val laneDelta = abs(entity.lane - lanePosition)
        val magnetRange = if (abilityActive && ability == MaskAbility.MAGNET) 1.25f else 0.55f
        val verticalDelta = abs(entity.y - runnerY)
        if (entity.isObstacle) {
            if (laneDelta > 0.55f || verticalDelta > GameConfig.HIT_BOX_HEIGHT_DP) {
                return entity
            }
            if (invulnerableMs > 0f) {
                return entity
            }
            if (abilityActive && ability == MaskAbility.GHOST) {
                return entity
            }
            if (!entity.isTall && isAirborne()) {
                return entity
            }
            lives -= 1
            combo = 0
            invulnerableMs = GameConfig.INVULNERABLE_MS.toFloat()
            events.add(RunEvent.HIT_TAKEN)
            return entity.copy(consumed = true)
        }
        if (laneDelta > magnetRange || verticalDelta > GameConfig.HIT_BOX_HEIGHT_DP) {
            return entity
        }
        if (entity.kind == RunEntity.Kind.CROWN) {
            crowns += 1
            combo += 1
            if (combo > bestCombo) {
                bestCombo = combo
            }
            events.add(RunEvent.CROWN_COLLECTED)
            if (combo > 0 && combo % GameConfig.COMBO_RIBBON_STEP == 0) {
                events.add(RunEvent.COMBO_REACHED)
            }
        } else {
            shards += 1
            events.add(RunEvent.SHARD_COLLECTED)
        }
        return entity.copy(consumed = true)
    }

    private fun advanceSpeed(deltaMs: Float) {
        rampAccumulatorMs += deltaMs
        if (rampAccumulatorMs >= GameConfig.SPEED_RAMP_INTERVAL_MS) {
            rampAccumulatorMs -= GameConfig.SPEED_RAMP_INTERVAL_MS
            speedFactor *= GameConfig.SPEED_RAMP_FACTOR
        }
    }

    private fun currentSpeedDpPerMs(): Float {
        val slow = if (abilityActive && ability == MaskAbility.SLOW) 0.55f else 1f
        val raw = GameConfig.BASE_SCROLL_DP_PER_SECOND *
            quest.speedFactor * difficultyMultiplier * speedFactor * slow
        return raw.coerceAtMost(GameConfig.MAX_SCROLL_DP_PER_SECOND) / 1000f
    }

    private fun advanceLane(deltaMs: Float) {
        if (lanePosition == targetLane.toFloat()) {
            return
        }
        laneAnimMs += deltaMs
        val fraction = (laneAnimMs / GameConfig.LANE_CHANGE_MS).coerceIn(0f, 1f)
        val eased = 1f - (1f - fraction) * (1f - fraction)
        lanePosition = laneStart + (targetLane - laneStart) * eased
        if (fraction >= 1f) {
            lanePosition = targetLane.toFloat()
        }
    }

    private fun advanceJump(deltaMs: Float) {
        if (jumpMs < 0f) {
            return
        }
        jumpMs += deltaMs
        if (jumpMs >= GameConfig.JUMP_DURATION_MS) {
            jumpMs = -1f
        }
    }

    private fun advanceAbility(deltaMs: Float) {
        if (abilityActiveMs > 0f) {
            abilityActiveMs -= deltaMs
            if (abilityActiveMs <= 0f) {
                abilityActiveMs = 0f
                abilityCooldownMs = ability.cooldownMs.toFloat()
            }
            return
        }
        if (abilityCooldownMs > 0f) {
            abilityCooldownMs -= deltaMs
            if (abilityCooldownMs < 0f) {
                abilityCooldownMs = 0f
            }
        }
    }

    fun snapshot(): RunSnapshot {
        val jumpProgress = if (jumpMs < 0f) {
            0f
        } else {
            sin((jumpMs / GameConfig.JUMP_DURATION_MS) * Math.PI).toFloat()
        }
        val flickerVisible = invulnerableMs <= 0f ||
            ((invulnerableMs / FLICKER_PERIOD_MS).toInt() % 2 == 0)
        return RunSnapshot(
            entities = ArrayList(entities),
            lanePosition = lanePosition,
            jumpProgress = jumpProgress,
            scrollPhase = scrollPhase,
            runnerVisible = flickerVisible,
            ghost = abilityActive && ability == MaskAbility.GHOST,
            magnet = abilityActive && ability == MaskAbility.MAGNET
        )
    }

    fun reset() {
        entities.clear()
        spawner.reset()
        elapsedMs = 0f
        rampAccumulatorMs = 0f
        speedFactor = 1f
        scrollPhase = 0f
        lanePosition = GameConfig.START_LANE.toFloat()
        targetLane = GameConfig.START_LANE
        laneStart = lanePosition
        laneAnimMs = 0f
        jumpMs = -1f
        invulnerableMs = 0f
        abilityActiveMs = 0f
        abilityCooldownMs = 0f
        lives = GameConfig.MAX_LIVES
        crowns = 0
        shards = 0
        combo = 0
        bestCombo = 0
        distanceDp = 0f
        finished = false
        completed = false
    }

    private companion object {
        const val SCROLL_PERIOD_DP = 120f
        const val FLICKER_PERIOD_MS = 125f
    }
}
