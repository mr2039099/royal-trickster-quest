package com.royaltrickster.quest.core.config

object GameConfig {

    const val LOADER_DURATION_MS = 8000L

    const val FRAME_INTERVAL_MS = 16L
    const val COUNTDOWN_STEPS = 3
    const val COUNTDOWN_STEP_MS = 600L

    const val MAX_LIVES = 3
    const val INVULNERABLE_MS = 1200L
    const val ROUND_CAP_MS = 150000L
    const val RESULT_DELAY_MS = 700L

    const val BASE_SCROLL_DP_PER_SECOND = 420f
    const val MAX_SCROLL_DP_PER_SECOND = 820f
    const val SPEED_RAMP_INTERVAL_MS = 15000L
    const val SPEED_RAMP_FACTOR = 1.08f

    const val PROP_SPAWN_START_MS = 1350L
    const val PROP_SPAWN_FLOOR_MS = 620L
    const val PROP_SPAWN_DECAY = 0.97f
    const val CROWN_SPAWN_MS = 850L
    const val SHARD_SPAWN_MS = 7000L

    const val LANE_COUNT = 3
    const val START_LANE = 1
    const val LANE_CHANGE_MS = 140L
    const val JUMP_DURATION_MS = 520L
    const val JUMP_HEIGHT_DP = 62f

    const val BOARD_MAX_WIDTH_DP = 380f
    const val BOARD_SIDE_GUTTER_DP = 32f
    const val BOARD_PADDING_DP = 10f
    const val BOARD_STROKE_DP = 2f
    const val RUNNER_SIZE_DP = 76f
    const val RUNNER_BOARD_FRACTION = 0.76f
    const val PROP_SIZE_DP = 64f
    const val PROP_SIDE_INSET_DP = 6f
    const val CROWN_SIZE_DP = 26f
    const val SHARD_SIZE_DP = 30f

    const val HIT_BOX_WIDTH_DP = 48f
    const val HIT_BOX_HEIGHT_DP = 56f
    const val COMBO_RIBBON_STEP = 5
    const val METERS_PER_DP = 0.05f
}
