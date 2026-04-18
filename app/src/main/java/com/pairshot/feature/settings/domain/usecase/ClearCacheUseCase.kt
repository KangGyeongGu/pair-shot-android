package com.pairshot.feature.settings.domain.usecase

import com.pairshot.feature.settings.domain.repository.StorageRepository
import javax.inject.Inject

class ClearCacheUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): Long = storageRepository.clearCache()
    }
