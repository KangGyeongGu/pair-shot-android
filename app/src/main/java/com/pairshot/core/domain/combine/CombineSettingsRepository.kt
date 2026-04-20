package com.pairshot.core.domain.combine

import kotlinx.coroutines.flow.Flow

interface CombineSettingsRepository {
    val configFlow: Flow<CombineConfig>

    suspend fun saveConfig(config: CombineConfig)

    suspend fun getConfig(): CombineConfig
}
