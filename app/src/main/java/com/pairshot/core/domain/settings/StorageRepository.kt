package com.pairshot.core.domain.settings

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo

    suspend fun clearCache(): Long
}
