package com.pairshot.feature.pair.domain.usecase

import com.pairshot.feature.pair.domain.repository.PhotoPairRepository
import javax.inject.Inject

class CombineImagesUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(pairId: Long): String = photoPairRepository.combinePair(pairId)
    }
