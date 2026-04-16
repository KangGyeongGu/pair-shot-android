package com.pairshot.domain.usecase.capture

import com.pairshot.domain.repository.PhotoPairRepository
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
