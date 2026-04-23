package com.pairshot.core.domain.settings

import com.pairshot.core.model.StorageInfo

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo

    suspend fun clearCache(): Long
}
