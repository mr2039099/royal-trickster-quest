package com.royaltrickster.quest.core.di

import android.content.Context
import com.royaltrickster.quest.data.local.ProgressPreferences
import com.royaltrickster.quest.data.local.SettingsPreferences
import com.royaltrickster.quest.data.repository.MaskRepositoryImpl
import com.royaltrickster.quest.data.repository.QuestRepositoryImpl
import com.royaltrickster.quest.data.repository.SettingsRepositoryImpl
import com.royaltrickster.quest.domain.repository.MaskRepository
import com.royaltrickster.quest.domain.repository.QuestRepository
import com.royaltrickster.quest.domain.repository.SettingsRepository
import com.royaltrickster.quest.domain.usecase.EquipMaskUseCase
import com.royaltrickster.quest.domain.usecase.GetPlayerProgressUseCase
import com.royaltrickster.quest.domain.usecase.GetQuestListUseCase
import com.royaltrickster.quest.domain.usecase.SaveRunResultUseCase
import com.royaltrickster.quest.domain.usecase.UnlockMasksUseCase

object ServiceLocator {

    private var initialised = false

    lateinit var questRepository: QuestRepository
        private set
    lateinit var maskRepository: MaskRepository
        private set
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var getQuestList: GetQuestListUseCase
        private set
    lateinit var getPlayerProgress: GetPlayerProgressUseCase
        private set
    lateinit var saveRunResult: SaveRunResultUseCase
        private set
    lateinit var equipMask: EquipMaskUseCase
        private set
    lateinit var unlockMasks: UnlockMasksUseCase
        private set

    fun init(context: Context) {
        if (initialised) {
            return
        }
        val appContext = context.applicationContext
        val questImpl = QuestRepositoryImpl(ProgressPreferences(appContext))
        val maskImpl = MaskRepositoryImpl(questImpl)
        questRepository = questImpl
        maskRepository = maskImpl
        settingsRepository = SettingsRepositoryImpl(SettingsPreferences(appContext))
        getQuestList = GetQuestListUseCase(questImpl)
        getPlayerProgress = GetPlayerProgressUseCase(questImpl)
        unlockMasks = UnlockMasksUseCase(maskImpl)
        saveRunResult = SaveRunResultUseCase(questImpl, unlockMasks)
        equipMask = EquipMaskUseCase(maskImpl)
        initialised = true
    }

    fun isReady(): Boolean = initialised
}
