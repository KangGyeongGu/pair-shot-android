package com.pairshot.core.domain.export

import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig

interface ExportRepository {
    suspend fun composeCombinedForGallery(
        pairIds: List<Long>,
        combineConfig: CombineConfig,
        watermarkConfig: WatermarkConfig?,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Int

    suspend fun saveWatermarkedOriginals(
        pairIds: List<Long>,
        preset: ExportPreset,
        watermarkConfig: WatermarkConfig,
        onProgress: (current: Int, total: Int) -> Unit,
    ): Int

    suspend fun buildShareablePayload(
        pairIds: List<Long>,
        preset: ExportPreset,
        combineConfig: CombineConfig,
        watermarkConfig: WatermarkConfig?,
        onProgress: (current: Int, total: Int) -> Unit,
    ): ExportAction
}
