package com.pairshot.feature.pair.domain.usecase

import com.pairshot.feature.pair.domain.model.PhotoPair
import com.pairshot.feature.pair.domain.repository.PhotoPairRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUnpairedPhotosUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        operator fun invoke(projectId: Long): Flow<List<PhotoPair>> = photoPairRepository.getUnpairedByProject(projectId)
    }
