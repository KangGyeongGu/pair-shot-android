package com.pairshot.core.domain.pair

import com.pairshot.core.domain.combine.CombineConfig
import com.pairshot.core.domain.settings.WatermarkConfig
import javax.inject.Inject

class CombineImagesUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(
            pairId: Long,
            watermarkConfig: WatermarkConfig? = null,
            combineConfigOverride: CombineConfig? = null,
        ): String = photoPairRepository.combinePair(pairId, watermarkConfig, combineConfigOverride)
    }
