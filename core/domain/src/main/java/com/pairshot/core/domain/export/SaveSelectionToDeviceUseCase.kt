package com.pairshot.core.domain.export

import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportFormat
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig
import javax.inject.Inject

class SaveSelectionToDeviceUseCase
    @Inject
    constructor(
        private val exportRepository: ExportRepository,
    ) {
        suspend operator fun invoke(
            pairIds: List<Long>,
            preset: ExportPreset,
            watermarkConfig: WatermarkConfig?,
            combineConfig: CombineConfig,
            outputUri: String?,
            onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
        ) {
            require(pairIds.isNotEmpty()) { "내보낼 페어가 없습니다" }
            require(preset.includeBefore || preset.includeAfter || preset.includeCombined) {
                "최소 하나의 항목을 포함해야 합니다"
            }

            when (preset.format) {
                ExportFormat.ZIP -> {
                    requireNotNull(outputUri) { "ZIP 저장 시 outputUri가 필요합니다" }
                    exportRepository.exportZipToDevice(
                        pairIds = pairIds,
                        outputUri = outputUri,
                        preset = preset,
                        combineConfig = combineConfig,
                        watermarkConfig = watermarkConfig,
                        onProgress = onProgress,
                    )
                }

                ExportFormat.INDIVIDUAL -> {
                    exportRepository.saveImagesToGallery(
                        pairIds = pairIds,
                        preset = preset,
                        combineConfig = combineConfig,
                        watermarkConfig = watermarkConfig,
                        onProgress = onProgress,
                    )
                }
            }
        }
    }
