package com.pairshot.core.domain.export

import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig
import javax.inject.Inject

class ExportZipUseCase
    @Inject
    constructor(
        private val exportRepository: ExportRepository,
    ) {
        suspend operator fun invoke(
            pairIds: List<Long>,
            outputUri: String,
            preset: ExportPreset,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ) {
            exportRepository.exportZipToDevice(
                pairIds = pairIds,
                outputUri = outputUri,
                preset = preset,
                combineConfig = combineConfig,
                watermarkConfig = watermarkConfig,
                onProgress = onProgress,
            )
        }
    }
