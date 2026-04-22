package com.pairshot.core.domain.combine

import com.pairshot.core.model.CombineHistory

interface CombineHistoryRepository {
    suspend fun getByPair(pairId: Long): CombineHistory?

    suspend fun findByPairIds(pairIds: List<Long>): Map<Long, CombineHistory>

    suspend fun upsert(entry: CombineHistory): Long

    suspend fun deleteCombinedPhoto(pairId: Long)
}
