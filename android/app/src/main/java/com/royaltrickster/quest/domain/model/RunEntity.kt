package com.royaltrickster.quest.domain.model

data class RunEntity(
    val id: Long,
    val kind: Kind,
    val lane: Int,
    val y: Float,
    val consumed: Boolean
) {
    enum class Kind {
        BARREL,
        CRATE,
        BANNER,
        PIKE,
        CROWN,
        SHARD
    }

    val isObstacle: Boolean
        get() = kind == Kind.BARREL || kind == Kind.CRATE ||
            kind == Kind.BANNER || kind == Kind.PIKE

    val isTall: Boolean
        get() = kind == Kind.BANNER || kind == Kind.PIKE
}
