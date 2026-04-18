package com.pairshot.feature.camera.domain.usecase

import com.pairshot.feature.pair.domain.repository.PhotoPairRepository
import javax.inject.Inject

class SaveAfterPhotoUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(
            pairId: Long,
            tempFileUri: String,
        ) {
            photoPairRepository.saveAfterPhoto(
                pairId = pairId,
                tempFileUri = tempFileUri,
            )
        }
    }
