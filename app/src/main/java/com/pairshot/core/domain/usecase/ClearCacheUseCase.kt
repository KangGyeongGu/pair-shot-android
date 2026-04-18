package com.pairshot.core.domain.usecase

import com.pairshot.core.domain.repository.StorageRepository
import javax.inject.Inject

class ClearCacheUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): Long = storageRepository.clearCache()
    }
