package com.pairshot.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pairshot.core.database.entity.PairAlbumCrossRefEntity
import com.pairshot.core.database.entity.PhotoPairEntity
import com.pairshot.core.database.entity.PhotoPairWithCountsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PairAlbumCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: PairAlbumCrossRefEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<PairAlbumCrossRefEntity>)

    @Query("DELETE FROM pair_album_cross_ref WHERE albumId = :albumId AND pairId IN (:pairIds)")
    suspend fun deleteAll(
        albumId: Long,
        pairIds: List<Long>,
    )

    @Query(
        """
        SELECT
            pp.*,
            EXISTS(SELECT 1 FROM combine_history ch WHERE ch.pairId = pp.id) AS hasCombined
        FROM photo_pairs pp
        INNER JOIN pair_album_cross_ref ref ON pp.id = ref.pairId
        WHERE ref.albumId = :albumId
        ORDER BY pp.beforeTimestamp ASC
        """,
    )
    fun getPairsByAlbum(albumId: Long): Flow<List<PhotoPairWithCountsEntity>>

    @Query(
        """
        SELECT pp.*
        FROM photo_pairs pp
        INNER JOIN pair_album_cross_ref ref ON pp.id = ref.pairId
        WHERE ref.albumId = :albumId AND pp.status = 'BEFORE_ONLY'
        ORDER BY pp.beforeTimestamp ASC
        """,
    )
    fun getUnpairedByAlbum(albumId: Long): Flow<List<PhotoPairEntity>>

    @Query("SELECT albumId FROM pair_album_cross_ref WHERE pairId = :pairId")
    suspend fun getAlbumIdsForPair(pairId: Long): List<Long>
}
