package com.pairshot.domain.repository

import com.pairshot.domain.model.PhotoPair
import kotlinx.coroutines.flow.Flow

interface PhotoPairRepository {
    fun getPairsByProject(projectId: Long): Flow<List<PhotoPair>>

    fun getUnpairedByProject(projectId: Long): Flow<List<PhotoPair>>

    suspend fun getById(id: Long): PhotoPair?

    suspend fun insert(pair: PhotoPair): Long

    suspend fun update(pair: PhotoPair)

    suspend fun delete(pair: PhotoPair)

    fun countByProject(projectId: Long): Flow<Int>

    suspend fun saveBeforePhoto(
        projectId: Long,
        tempFileUri: String,
        zoomLevel: Float?,
        lensId: String?,
    ): Long
}
