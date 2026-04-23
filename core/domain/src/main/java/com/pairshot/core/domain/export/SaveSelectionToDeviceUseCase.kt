package com.pairshot.core.domain.export

import com.pairshot.core.model.CombineConfig
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
            onProgress: (current: Int, total: Int) -> Unit = { _, _ -> },
        ): Int {
            require(pairIds.isNotEmpty()) { "no pairs to export" }

            val effectiveCombine = if (preset.applyCombineConfig) combineConfig else CombineConfig()

            val combinedCount =
                if (preset.includeCombined) {
                    exportRepository.composeCombinedForGallery(
                        pairIds = pairIds,
                        combineConfig = effectiveCombine,
                        watermarkConfig = watermarkConfig,
                        onProgress = onProgress,
                    )
                } else {
                    0
                }

            val watermarkedCount =
                if (watermarkConfig != null && (preset.includeBefore || preset.includeAfter)) {
                    exportRepository.saveWatermarkedOriginals(
                        pairIds = pairIds,
                        preset = preset,
                        watermarkConfig = watermarkConfig,
                        onProgress = onProgress,
                    )
                } else {
                    0
                }

            return combinedCount + watermarkedCount
        }
    }
