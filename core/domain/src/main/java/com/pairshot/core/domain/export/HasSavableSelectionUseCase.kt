package com.pairshot.core.domain.export

import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig
import javax.inject.Inject

class HasSavableSelectionUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(
            pairIds: List<Long>,
            preset: ExportPreset,
            watermarkConfig: WatermarkConfig?,
        ): Boolean {
            if (pairIds.isEmpty()) return false
            val pairs = photoPairRepository.getByIds(pairIds)
            return pairs.any { pair ->
                val beforeValid = pair.beforePhotoUri.isNotBlank()
                val afterValid = !pair.afterPhotoUri.isNullOrBlank()
                val canCombined = preset.includeCombined && beforeValid && afterValid
                val canWatermarkBefore = watermarkConfig != null && preset.includeBefore && beforeValid
                val canWatermarkAfter = watermarkConfig != null && preset.includeAfter && afterValid
                canCombined || canWatermarkBefore || canWatermarkAfter
            }
        }
    }
