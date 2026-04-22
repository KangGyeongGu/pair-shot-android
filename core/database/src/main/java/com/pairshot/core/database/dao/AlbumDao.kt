package com.pairshot.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pairshot.core.database.entity.AlbumEntity
import com.pairshot.core.database.entity.AlbumWithCountsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query(
        """
        SELECT
            a.*,
            (
                SELECT COUNT(*)
                FROM pair_album_cross_ref ref
                WHERE ref.albumId = a.id
            ) AS pairCount
        FROM albums a
        ORDER BY a.updatedAt DESC
        """,
    )
    fun getAllByUpdated(): Flow<List<AlbumWithCountsEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getById(id: Long): AlbumEntity?

    @Insert
    suspend fun insert(album: AlbumEntity): Long

    @Update
    suspend fun update(album: AlbumEntity)

    @Delete
    suspend fun delete(album: AlbumEntity)
}
