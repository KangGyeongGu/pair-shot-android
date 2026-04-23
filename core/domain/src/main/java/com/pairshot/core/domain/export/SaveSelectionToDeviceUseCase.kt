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
            require(pairIds.isNotEmpty()) { "no pairs to export" }
            require(preset.includeBefore || preset.includeAfter || preset.includeCombined) {
                "at least one include option is required"
            }

            when (preset.format) {
                ExportFormat.ZIP -> {
                    requireNotNull(outputUri) { "outputUri is required for ZIP export" }
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
