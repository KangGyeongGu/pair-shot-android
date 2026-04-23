package com.pairshot.core.domain.export

import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportFormat
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig
import javax.inject.Inject

sealed interface ExportAction {
    data class ShareImages(
        val uris: List<String>,
    ) : ExportAction

    data class ShareZip(
        val filePath: String,
    ) : ExportAction
}

class ShareSelectionUseCase
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
        ): ExportAction {
            require(pairIds.isNotEmpty()) { "no pairs to share" }
            require(preset.includeBefore || preset.includeAfter || preset.includeCombined) {
                "at least one include option is required"
            }

            return when (preset.format) {
                ExportFormat.ZIP -> {
                    val filePath =
                        exportRepository.createShareableZip(
                            pairIds = pairIds,
                            preset = preset,
                            combineConfig = combineConfig,
                            watermarkConfig = watermarkConfig,
                            onProgress = onProgress,
                        )
                    ExportAction.ShareZip(filePath)
                }

                ExportFormat.INDIVIDUAL -> {
                    val uris =
                        exportRepository.prepareShareableImages(
                            pairIds = pairIds,
                            preset = preset,
                            combineConfig = combineConfig,
                            watermarkConfig = watermarkConfig,
                            onProgress = onProgress,
                        )
                    ExportAction.ShareImages(uris)
                }
            }
        }
    }
