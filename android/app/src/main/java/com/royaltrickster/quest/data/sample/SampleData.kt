package com.royaltrickster.quest.data.sample

import com.royaltrickster.quest.domain.model.Mask
import com.royaltrickster.quest.domain.model.MaskAbility
import com.royaltrickster.quest.domain.model.PlayerProgress
import com.royaltrickster.quest.domain.model.Quest

object SampleData {

    const val MASK_JESTER = "jester"
    const val MASK_HARLEQUIN = "harlequin"
    const val MASK_GILDED = "gilded_crown"
    const val MASK_PHANTOM = "phantom"

    const val PROP_BARREL = "barrel"
    const val PROP_CRATE = "crate"
    const val PROP_BANNER = "banner"
    const val PROP_PIKE = "pike"

    val quests: List<Quest> = listOf(
        Quest(1, "Courtyard Welcome", 40, 0.90f, listOf(PROP_BARREL, PROP_CRATE)),
        Quest(2, "Lantern Walk", 70, 1.00f, listOf(PROP_BARREL, PROP_CRATE, PROP_BANNER)),
        Quest(3, "Banner Hall", 110, 1.08f, listOf(PROP_BANNER, PROP_PIKE, PROP_BARREL)),
        Quest(4, "Guard Gauntlet", 150, 1.16f, listOf(PROP_PIKE, PROP_CRATE, PROP_BANNER)),
        Quest(5, "Masquerade Stair", 195, 1.24f, listOf(PROP_BARREL, PROP_CRATE, PROP_BANNER, PROP_PIKE)),
        Quest(6, "Grand Parade", 250, 1.32f, listOf(PROP_BARREL, PROP_CRATE, PROP_BANNER, PROP_PIKE))
    )

    val masks: List<Mask> = listOf(
        Mask(MASK_JESTER, "Jester", "The starter grin", 0, MaskAbility.NONE),
        Mask(MASK_HARLEQUIN, "Harlequin", "Slows the parade to a stroll", 15, MaskAbility.SLOW),
        Mask(MASK_GILDED, "Gilded Crown", "Draws every crown to you", 30, MaskAbility.MAGNET),
        Mask(MASK_PHANTOM, "Phantom", "Walk straight through the props", 50, MaskAbility.GHOST)
    )

    const val SEEDED_BEST_DISTANCE = 1480
    const val SEEDED_BEST_COMBO = 12

    fun defaultProgress(): PlayerProgress = PlayerProgress(
        crowns = 0,
        shards = 0,
        currentQuest = 1,
        completedQuests = 0,
        equippedMaskId = MASK_JESTER,
        unlockedMaskIds = setOf(MASK_JESTER),
        bestDistanceMeters = 0,
        bestCombo = 0,
        recovered = false
    )

    fun questByNumber(number: Int): Quest {
        val index = (number - 1).coerceIn(0, quests.lastIndex)
        return quests[index]
    }

    fun maskById(id: String): Mask = masks.firstOrNull { it.id == id } ?: masks.first()
}
