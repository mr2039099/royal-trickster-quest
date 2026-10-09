package com.royaltrickster.quest.domain.usecase

import com.royaltrickster.quest.domain.repository.MaskRepository

class EquipMaskUseCase(private val repository: MaskRepository) {

    operator fun invoke(maskId: String): Boolean {
        if (!repository.unlockedIds().contains(maskId)) {
            return false
        }
        repository.equip(maskId)
        return true
    }
}
