package com.pairshot.core.domain.pair

import com.pairshot.core.model.PhotoPair

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnpairedPhotosUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        operator fun invoke(projectId: Long): Flow<List<PhotoPair>> = photoPairRepository.getUnpairedByProject(projectId)
    }
