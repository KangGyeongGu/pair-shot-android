package com.pairshot.domain.usecase.storage

import com.pairshot.domain.model.StorageInfo
import com.pairshot.domain.repository.StorageRepository
import javax.inject.Inject

class GetStorageInfoUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): StorageInfo = storageRepository.getStorageInfo()
    }
