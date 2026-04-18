package com.pairshot.core.domain.usecase

import com.pairshot.core.domain.model.PhotoPair
import com.pairshot.core.domain.repository.PhotoPairRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPairsByProjectUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        operator fun invoke(projectId: Long): Flow<List<PhotoPair>> = photoPairRepository.getPairsByProject(projectId)
    }
