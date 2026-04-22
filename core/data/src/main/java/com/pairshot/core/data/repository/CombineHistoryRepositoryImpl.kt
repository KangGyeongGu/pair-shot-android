package com.pairshot.core.data.repository

import android.net.Uri
import com.pairshot.core.database.dao.CombineHistoryDao
import com.pairshot.core.database.entity.toDomain
import com.pairshot.core.database.entity.toEntity
import com.pairshot.core.domain.combine.CombineHistoryRepository
import com.pairshot.core.model.CombineHistory
import com.pairshot.core.storage.MediaStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CombineHistoryRepositoryImpl
    @Inject
    constructor(
        private val combineHistoryDao: CombineHistoryDao,
        private val mediaStoreManager: MediaStoreManager,
    ) : CombineHistoryRepository {
        override suspend fun getByPair(pairId: Long): CombineHistory? =
            withContext(Dispatchers.IO) {
                combineHistoryDao.getByPair(pairId)?.toDomain()
            }

        override suspend fun findByPairIds(pairIds: List<Long>): Map<Long, CombineHistory> =
            withContext(Dispatchers.IO) {
                if (pairIds.isEmpty()) {
                    emptyMap()
                } else {
                    combineHistoryDao.findByPairIds(pairIds).associate { it.pairId to it.toDomain() }
                }
            }

        override suspend fun upsert(entry: CombineHistory): Long =
            withContext(Dispatchers.IO) {
                combineHistoryDao.upsert(entry.toEntity())
            }

        override suspend fun deleteCombinedPhoto(pairId: Long) {
            withContext(Dispatchers.IO) {
                val entry = combineHistoryDao.getByPair(pairId) ?: return@withContext
                runCatching { mediaStoreManager.deleteFromGallery(Uri.parse(entry.mediaStoreUri)) }
                combineHistoryDao.deleteByPair(pairId)
            }
        }
    }
