package com.pairshot.core.domain.usecase

import com.pairshot.core.domain.repository.PhotoPairRepository
import javax.inject.Inject

class SaveBeforePhotoUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(
            projectId: Long,
            tempFileUri: String,
            zoomLevel: Float?,
            lensId: String?,
        ): Long =
            photoPairRepository.saveBeforePhoto(
                projectId = projectId,
                tempFileUri = tempFileUri,
                zoomLevel = zoomLevel,
                lensId = lensId,
            )
    }
