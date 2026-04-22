package com.pairshot.core.domain.pair

import com.pairshot.core.model.PhotoPair
import kotlinx.coroutines.flow.Flow

interface PhotoPairRepository {
    fun observeAll(): Flow<List<PhotoPair>>

    fun observeUnpaired(): Flow<List<PhotoPair>>

    fun observeUnpairedByAlbum(albumId: Long): Flow<List<PhotoPair>>

    suspend fun getById(id: Long): PhotoPair?

    suspend fun delete(pair: PhotoPair)

    fun countAll(): Flow<Int>

    suspend fun saveBeforePhoto(
        tempFileUri: String,
        zoomLevel: Float?,
        albumId: Long? = null,
    ): Long

    suspend fun saveAfterPhoto(
        pairId: Long,
        tempFileUri: String,
    )
}
