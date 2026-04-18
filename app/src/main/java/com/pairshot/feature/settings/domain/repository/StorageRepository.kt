package com.pairshot.feature.settings.domain.repository

import com.pairshot.feature.settings.domain.model.StorageInfo

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo

    suspend fun clearCache(): Long
}
