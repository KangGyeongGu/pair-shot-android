package com.pairshot.feature.settings.domain.usecase

import com.pairshot.feature.settings.domain.model.StorageInfo
import com.pairshot.feature.settings.domain.repository.StorageRepository
import javax.inject.Inject

class GetStorageInfoUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): StorageInfo = storageRepository.getStorageInfo()
    }
