package com.pairshot.core.domain.combine

import com.pairshot.core.model.ExportHistoryEntry
import com.pairshot.core.model.ExportHistoryKind

interface ExportHistoryRepository {
    suspend fun insert(entry: ExportHistoryEntry): Long

    suspend fun findByPairIds(pairIds: List<Long>): List<ExportHistoryEntry>

    suspend fun findByPairIdsAndKind(
        pairIds: List<Long>,
        kind: ExportHistoryKind,
    ): List<ExportHistoryEntry>

    suspend fun deleteByPairIds(pairIds: List<Long>)

    suspend fun deleteByPairIdsAndKind(
        pairIds: List<Long>,
        kind: ExportHistoryKind,
    )
}
