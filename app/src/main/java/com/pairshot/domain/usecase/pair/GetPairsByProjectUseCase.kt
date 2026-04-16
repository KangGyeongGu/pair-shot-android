package com.pairshot.domain.usecase.pair

import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.repository.PhotoPairRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPairsByProjectUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
    ) {
        operator fun invoke(projectId: Long): Flow<List<PhotoPair>> = photoPairRepository.getPairsByProject(projectId)
    }
