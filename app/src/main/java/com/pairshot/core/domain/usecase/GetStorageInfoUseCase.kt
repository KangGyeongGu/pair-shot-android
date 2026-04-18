package com.pairshot.core.domain.usecase

import com.pairshot.core.domain.model.StorageInfo
import com.pairshot.core.domain.repository.StorageRepository
import javax.inject.Inject

class GetStorageInfoUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): StorageInfo = storageRepository.getStorageInfo()
    }
