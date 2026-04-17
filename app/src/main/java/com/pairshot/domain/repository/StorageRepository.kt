package com.pairshot.domain.repository

import com.pairshot.domain.model.StorageInfo

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo

    suspend fun clearCache(): Long
}
