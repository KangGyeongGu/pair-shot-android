package com.pairshot.core.domain.repository

import com.pairshot.core.domain.model.WatermarkConfig
import kotlinx.coroutines.flow.Flow

interface WatermarkRepository {
    val watermarkConfigFlow: Flow<WatermarkConfig>

    suspend fun saveConfig(config: WatermarkConfig)

    suspend fun getConfig(): WatermarkConfig

    suspend fun saveLogoFile(sourceUri: String): String
}
