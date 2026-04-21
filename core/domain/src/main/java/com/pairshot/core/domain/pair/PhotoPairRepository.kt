package com.pairshot.core.domain.pair

import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.model.WatermarkConfig
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

    suspend fun combinePair(
        pairId: Long,
        watermarkConfig: WatermarkConfig? = null,
        combineConfigOverride: CombineConfig? = null,
    ): String

    suspend fun getAllByProjectOnce(projectId: Long): List<PhotoPair>

    suspend fun getAll(): List<PhotoPair>

    suspend fun checkUrisExist(uris: List<String>): Set<String>
}
