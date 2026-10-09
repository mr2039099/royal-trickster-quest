package com.royaltrickster.quest.presentation.game

import com.royaltrickster.quest.core.config.GameConfig
import com.royaltrickster.quest.data.sample.SampleData
import com.royaltrickster.quest.domain.model.Quest
import com.royaltrickster.quest.domain.model.RunEntity
import kotlin.random.Random

class PropSpawner(
    private val quest: Quest,
    private val random: Random
) {

    private var rowIndex = 0
    private var propIntervalMs = GameConfig.PROP_SPAWN_START_MS.toFloat()
    private var propTimerMs = 900f
    private var crownTimerMs = 420f
    private var shardTimerMs = GameConfig.SHARD_SPAWN_MS.toFloat()
    private var nextId = 1L
    private var lastBlockedLane = -1

    fun reset() {
        rowIndex = 0
        propIntervalMs = GameConfig.PROP_SPAWN_START_MS.toFloat()
        propTimerMs = 900f
        crownTimerMs = 420f
        shardTimerMs = GameConfig.SHARD_SPAWN_MS.toFloat()
        lastBlockedLane = -1
    }

    fun advance(deltaMs: Float, spawnY: Float): List<RunEntity> {
        val spawned = ArrayList<RunEntity>(3)
        propTimerMs -= deltaMs
        crownTimerMs -= deltaMs
        shardTimerMs -= deltaMs
        if (propTimerMs <= 0f) {
            propTimerMs += propIntervalMs
            propIntervalMs = (propIntervalMs * GameConfig.PROP_SPAWN_DECAY)
                .coerceAtLeast(GameConfig.PROP_SPAWN_FLOOR_MS.toFloat())
            spawned.add(spawnProp(spawnY))
        }
        if (crownTimerMs <= 0f) {
            crownTimerMs += GameConfig.CROWN_SPAWN_MS.toFloat()
            spawned.add(spawnCollectible(RunEntity.Kind.CROWN, spawnY))
        }
        if (shardTimerMs <= 0f) {
            shardTimerMs += GameConfig.SHARD_SPAWN_MS.toFloat()
            spawned.add(spawnCollectible(RunEntity.Kind.SHARD, spawnY))
        }
        return spawned
    }

    private fun spawnProp(spawnY: Float): RunEntity {
        val lane = chooseBlockedLane()
        lastBlockedLane = lane
        rowIndex++
        return RunEntity(
            id = nextId++,
            kind = kindFor(),
            lane = lane,
            y = spawnY,
            consumed = false
        )
    }

    private fun chooseBlockedLane(): Int {
        if (rowIndex % CENTER_BLOCK_PERIOD == CENTER_BLOCK_PERIOD - 1) {
            return GameConfig.START_LANE
        }
        val options = (0 until GameConfig.LANE_COUNT).filter { it != lastBlockedLane }
        if (options.isEmpty()) {
            return random.nextInt(GameConfig.LANE_COUNT)
        }
        return options[random.nextInt(options.size)]
    }

    private fun spawnCollectible(kind: RunEntity.Kind, spawnY: Float): RunEntity {
        val options = (0 until GameConfig.LANE_COUNT).filter { it != lastBlockedLane }
        val lane = if (options.isEmpty()) {
            random.nextInt(GameConfig.LANE_COUNT)
        } else {
            options[random.nextInt(options.size)]
        }
        return RunEntity(
            id = nextId++,
            kind = kind,
            lane = lane,
            y = spawnY - COLLECTIBLE_LEAD_DP,
            consumed = false
        )
    }

    private fun kindFor(): RunEntity.Kind {
        val mix = quest.propMix
        if (mix.isEmpty()) {
            return RunEntity.Kind.BARREL
        }
        return when (mix[random.nextInt(mix.size)]) {
            SampleData.PROP_CRATE -> RunEntity.Kind.CRATE
            SampleData.PROP_BANNER -> RunEntity.Kind.BANNER
            SampleData.PROP_PIKE -> RunEntity.Kind.PIKE
            else -> RunEntity.Kind.BARREL
        }
    }

    private companion object {
        const val CENTER_BLOCK_PERIOD = 3
        const val COLLECTIBLE_LEAD_DP = 140f
    }
}
