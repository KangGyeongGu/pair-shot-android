package com.pairshot.core.domain.repository

import com.pairshot.core.domain.model.StorageInfo

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo

    suspend fun clearCache(): Long
}
