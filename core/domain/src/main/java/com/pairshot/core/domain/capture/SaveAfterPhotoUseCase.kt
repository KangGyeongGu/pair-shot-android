package com.pairshot.core.domain.capture

import com.pairshot.core.domain.pair.PhotoPairRepository
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
