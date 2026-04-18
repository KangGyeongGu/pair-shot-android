package com.pairshot.core.domain.pair

import javax.inject.Inject

class CombineImagesUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(pairId: Long): String = photoPairRepository.combinePair(pairId)
    }
