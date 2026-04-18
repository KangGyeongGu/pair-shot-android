package com.pairshot.feature.settings.domain.repository

import com.pairshot.feature.settings.domain.model.WatermarkConfig
import kotlinx.coroutines.flow.Flow

interface WatermarkRepository {
    val watermarkConfigFlow: Flow<WatermarkConfig>

    suspend fun saveConfig(config: WatermarkConfig)

    suspend fun getConfig(): WatermarkConfig

    /** 로고 파일을 앱 내부 저장소에 복사하고 절대 경로를 반환합니다. */
    suspend fun saveLogoFile(sourceUri: String): String
}
