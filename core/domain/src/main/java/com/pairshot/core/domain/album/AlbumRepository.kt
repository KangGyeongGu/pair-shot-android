package com.pairshot.core.domain.album

import com.pairshot.core.model.Album
import com.pairshot.core.model.PhotoPair
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun getAll(): Flow<List<Album>>

    suspend fun getById(id: Long): Album?

    fun observePairs(albumId: Long): Flow<List<PhotoPair>>

    suspend fun create(album: Album): Long

    suspend fun update(album: Album)

    suspend fun delete(albumId: Long)

    suspend fun addPairs(
        albumId: Long,
        pairIds: List<Long>,
    )

    suspend fun removePairs(
        albumId: Long,
        pairIds: List<Long>,
    )

    suspend fun getAlbumIdsForPair(pairId: Long): List<Long>
}
