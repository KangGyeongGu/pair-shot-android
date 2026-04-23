package com.pairshot.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pairshot.core.database.entity.ExportHistoryEntity

@Dao
interface ExportHistoryDao {
    @Insert
    suspend fun insert(entry: ExportHistoryEntity): Long

    @Query("SELECT * FROM export_history WHERE pairId IN (:pairIds)")
    suspend fun findByPairIds(pairIds: List<Long>): List<ExportHistoryEntity>

    @Query("SELECT * FROM export_history WHERE pairId IN (:pairIds) AND kind = :kind")
    suspend fun findByPairIdsAndKind(
        pairIds: List<Long>,
        kind: String,
    ): List<ExportHistoryEntity>

    @Query("DELETE FROM export_history WHERE pairId IN (:pairIds)")
    suspend fun deleteByPairIds(pairIds: List<Long>)

    @Query("DELETE FROM export_history WHERE pairId IN (:pairIds) AND kind = :kind")
    suspend fun deleteByPairIdsAndKind(
        pairIds: List<Long>,
        kind: String,
    )
}
