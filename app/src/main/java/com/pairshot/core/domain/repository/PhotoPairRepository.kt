package com.pairshot.core.domain.repository

import com.pairshot.core.domain.model.PhotoPair
import kotlinx.coroutines.flow.Flow

interface PhotoPairRepository {
    fun getPairsByProject(projectId: Long): Flow<List<PhotoPair>>

    fun getUnpairedByProject(projectId: Long): Flow<List<PhotoPair>>

    suspend fun getById(id: Long): PhotoPair?

    suspend fun insert(pair: PhotoPair): Long

    suspend fun update(pair: PhotoPair)

    suspend fun delete(pair: PhotoPair)

    fun countByProject(projectId: Long): Flow<Int>

    suspend fun resetAfterPhoto(pairId: Long)

    suspend fun saveBeforePhoto(
        projectId: Long,
        tempFileUri: String,
        zoomLevel: Float?,
        lensId: String?,
    ): Long

    suspend fun saveAfterPhoto(
        pairId: Long,
        tempFileUri: String,
    )

    suspend fun removeCombinedPhoto(pairId: Long)

    suspend fun combinePair(pairId: Long): String

    suspend fun getAllByProjectOnce(projectId: Long): List<PhotoPair>

    suspend fun getAll(): List<PhotoPair>

    suspend fun checkUrisExist(uris: List<String>): Set<String>
}
