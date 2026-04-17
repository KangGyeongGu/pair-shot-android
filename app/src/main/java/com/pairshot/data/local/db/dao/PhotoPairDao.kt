package com.pairshot.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pairshot.data.local.db.entity.PhotoPairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoPairDao {
    @Query("SELECT * FROM photo_pairs WHERE projectId = :projectId ORDER BY beforeTimestamp ASC")
    fun getPairsByProject(projectId: Long): Flow<List<PhotoPairEntity>>

    @Query(
        "SELECT * FROM photo_pairs " +
            "WHERE projectId = :projectId AND status = 'BEFORE_ONLY' " +
            "ORDER BY beforeTimestamp ASC",
    )
    fun getUnpairedByProject(projectId: Long): Flow<List<PhotoPairEntity>>

    @Query("SELECT * FROM photo_pairs WHERE id = :id")
    suspend fun getById(id: Long): PhotoPairEntity?

    @Query("SELECT * FROM photo_pairs WHERE id IN (:ids) ORDER BY beforeTimestamp ASC")
    suspend fun getByIds(ids: List<Long>): List<PhotoPairEntity>

    @Insert
    suspend fun insert(pair: PhotoPairEntity): Long

    @Update
    suspend fun update(pair: PhotoPairEntity)

    @Delete
    suspend fun delete(pair: PhotoPairEntity)

    @Query("SELECT COUNT(*) FROM photo_pairs WHERE projectId = :projectId")
    fun countByProject(projectId: Long): Flow<Int>

    @Query("SELECT * FROM photo_pairs WHERE projectId = :projectId")
    suspend fun getAllByProjectOnce(projectId: Long): List<PhotoPairEntity>
}
