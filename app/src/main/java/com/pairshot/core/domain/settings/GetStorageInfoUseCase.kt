package com.pairshot.core.domain.settings

import javax.inject.Inject

class GetStorageInfoUseCase
    @Inject
    constructor(
        private val storageRepository: StorageRepository,
    ) {
        suspend operator fun invoke(): StorageInfo = storageRepository.getStorageInfo()
    }
