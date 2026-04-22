package com.pairshot.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pairshot.core.database.entity.CombineHistoryEntity

@Dao
interface CombineHistoryDao {
    @Query("SELECT * FROM combine_history WHERE pairId = :pairId LIMIT 1")
    suspend fun getByPair(pairId: Long): CombineHistoryEntity?

    @Query("SELECT * FROM combine_history WHERE pairId IN (:pairIds)")
    suspend fun findByPairIds(pairIds: List<Long>): List<CombineHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CombineHistoryEntity): Long

    @Query("DELETE FROM combine_history WHERE pairId = :pairId")
    suspend fun deleteByPair(pairId: Long)
}
