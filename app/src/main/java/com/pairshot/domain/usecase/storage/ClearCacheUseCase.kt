package com.pairshot.domain.usecase.storage

import com.pairshot.domain.repository.StorageRepository
import javax.inject.Inject

class ClearCacheUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): Long = storageRepository.clearCache()
    }
