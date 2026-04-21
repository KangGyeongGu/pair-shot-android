package com.pairshot.data.repository.pair

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.FileNameGenerator
import com.pairshot.core.rendering.PairImageComposer
import com.pairshot.core.database.dao.PhotoPairDao
import com.pairshot.core.database.dao.ProjectDao
import com.pairshot.core.database.entity.PhotoPairEntity
import com.pairshot.core.database.entity.toDomain
import com.pairshot.core.database.entity.toEntity
import com.pairshot.core.storage.MediaStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PhotoPairRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val photoPairDao: PhotoPairDao,
        private val projectDao: ProjectDao,
        private val mediaStoreManager: MediaStoreManager,
        private val fileNameGenerator: FileNameGenerator,
        private val pairImageComposer: PairImageComposer,
        private val appSettingsRepository: AppSettingsRepository,
        private val combineSettingsRepository: CombineSettingsRepository,
    ) : PhotoPairRepository {
        override fun getPairsByProject(projectId: Long): Flow<List<PhotoPair>> =
            photoPairDao.getPairsByProject(projectId).map { entities ->
                entities.map { it.toDomain() }
            }

        override fun getUnpairedByProject(projectId: Long): Flow<List<PhotoPair>> =
            photoPairDao.getUnpairedByProject(projectId).map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun getById(id: Long): PhotoPair? =
            withContext(Dispatchers.IO) {
                photoPairDao.getById(id)?.toDomain()
            }

        override suspend fun insert(pair: PhotoPair): Long =
            withContext(Dispatchers.IO) {
                photoPairDao.insert(pair.toEntity())
            }

        override suspend fun update(pair: PhotoPair) =
            withContext(Dispatchers.IO) {
                photoPairDao.update(pair.toEntity())
                projectDao.updateTimestamp(pair.projectId, System.currentTimeMillis())
            }

        override suspend fun delete(pair: PhotoPair) =
            withContext(Dispatchers.IO) {
                mediaStoreManager.deleteFromGallery(Uri.parse(pair.beforePhotoUri))
                pair.afterPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                pair.combinedPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                photoPairDao.delete(pair.toEntity())
                projectDao.updateTimestamp(pair.projectId, System.currentTimeMillis())
            }

        override suspend fun resetAfterPhoto(pairId: Long) =
            withContext(Dispatchers.IO) {
                val entity =
                    photoPairDao.getById(pairId)
                        ?: throw IllegalArgumentException("PhotoPair not found: $pairId")
                entity.afterPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                entity.combinedPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                photoPairDao.update(
                    entity.copy(
                        afterPhotoUri = null,
                        afterTimestamp = null,
                        combinedPhotoUri = null,
                        status = PairStatus.BEFORE_ONLY.name,
                    ),
                )
                projectDao.updateTimestamp(entity.projectId, System.currentTimeMillis())
            }

        override suspend fun removeCombinedPhoto(pairId: Long) =
            withContext(Dispatchers.IO) {
                val entity =
                    photoPairDao.getById(pairId)
                        ?: throw IllegalArgumentException("PhotoPair not found: $pairId")
                entity.combinedPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                photoPairDao.update(
                    entity.copy(
                        combinedPhotoUri = null,
                        status = PairStatus.PAIRED.name,
                    ),
                )
                projectDao.updateTimestamp(entity.projectId, System.currentTimeMillis())
            }

        override fun countByProject(projectId: Long): Flow<Int> = photoPairDao.countByProject(projectId)

        override suspend fun getAllByProjectOnce(projectId: Long): List<PhotoPair> =
            withContext(Dispatchers.IO) {
                photoPairDao.getAllByProjectOnce(projectId).map { it.toDomain() }
            }

        override suspend fun getAll(): List<PhotoPair> =
            withContext(Dispatchers.IO) {
                photoPairDao.getAll().map { it.toDomain() }
            }

        override suspend fun checkUrisExist(uris: List<String>): Set<String> =
            withContext(Dispatchers.IO) {
                val existingUris = mutableSetOf<String>()
                uris.forEach { uri ->
                    try {
                        val contentUri = Uri.parse(uri)
                        context.contentResolver
                            .query(
                                contentUri,
                                arrayOf(MediaStore.Images.Media._ID),
                                null,
                                null,
                                null,
                            )?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    existingUris.add(uri)
                                }
                            }
                    } catch (e: Exception) {
                        Timber.d(e, "URI 존재 확인 실패: $uri")
                    }
                }
                existingUris
            }

        override suspend fun saveBeforePhoto(
            projectId: Long,
            tempFileUri: String,
            zoomLevel: Float?,
            lensId: String?,
        ): Long =
            withContext(Dispatchers.IO) {
                val project =
                    projectDao.getById(projectId)
                        ?: throw IllegalArgumentException("Project not found: $projectId")

                val currentCount = photoPairDao.countByProject(projectId).first()
                val sequenceNumber = currentCount + 1

                val prefix = appSettingsRepository.settingsFlow.first().fileNamePrefix
                val fileName = fileNameGenerator.generateBeforeFileName(sequenceNumber, prefix)

                val savedUri =
                    mediaStoreManager.saveToGallery(
                        tempFileUri = Uri.parse(tempFileUri),
                        projectName = project.name,
                        displayName = fileName,
                    )

                val entity =
                    PhotoPairEntity(
                        projectId = projectId,
                        beforePhotoUri = savedUri.toString(),
                        beforeTimestamp = System.currentTimeMillis(),
                        status = PairStatus.BEFORE_ONLY.name,
                        zoomLevel = zoomLevel,
                        lensId = lensId,
                    )
                try {
                    val pairId = photoPairDao.insert(entity)
                    projectDao.updateTimestamp(projectId, System.currentTimeMillis())
                    pairId
                } catch (e: Exception) {
                    mediaStoreManager.deleteFromGallery(savedUri)
                    throw e
                }
            }

        override suspend fun saveAfterPhoto(
            pairId: Long,
            tempFileUri: String,
        ) = withContext(Dispatchers.IO) {
            val entity =
                photoPairDao.getById(pairId)
                    ?: throw IllegalArgumentException("PhotoPair not found: $pairId")
            val project =
                projectDao.getById(entity.projectId)
                    ?: throw IllegalArgumentException("Project not found: ${entity.projectId}")

            entity.afterPhotoUri?.let {
                try {
                    mediaStoreManager.deleteFromGallery(Uri.parse(it))
                } catch (e: Exception) {
                    Timber.w(e, "이전 After 사진 삭제 실패: $it")
                }
            }
            entity.combinedPhotoUri?.let {
                try {
                    mediaStoreManager.deleteFromGallery(Uri.parse(it))
                } catch (e: Exception) {
                    Timber.w(e, "이전 합성 사진 삭제 실패: $it")
                }
            }

            val sequenceNumber = extractSequenceNumber(entity.beforePhotoUri)
            val prefix = appSettingsRepository.settingsFlow.first().fileNamePrefix
            val fileName = fileNameGenerator.generateAfterFileName(sequenceNumber, prefix)

            val savedUri =
                mediaStoreManager.saveToGallery(
                    tempFileUri = Uri.parse(tempFileUri),
                    projectName = project.name,
                    displayName = fileName,
                )

            try {
                val now = System.currentTimeMillis()
                photoPairDao.update(
                    entity.copy(
                        afterPhotoUri = savedUri.toString(),
                        afterTimestamp = now,
                        combinedPhotoUri = null,
                        status = PairStatus.PAIRED.name,
                    ),
                )
                projectDao.updateTimestamp(entity.projectId, now)
            } catch (e: Exception) {
                mediaStoreManager.deleteFromGallery(savedUri)
                throw e
            }
        }

        override suspend fun combinePair(
            pairId: Long,
            watermarkConfig: WatermarkConfig?,
            combineConfigOverride: CombineConfig?,
        ): String =
            withContext(Dispatchers.IO) {
                val entity =
                    photoPairDao.getById(pairId)
                        ?: throw IllegalArgumentException("PhotoPair not found: $pairId")
                val project =
                    projectDao.getById(entity.projectId)
                        ?: throw IllegalArgumentException("Project not found: ${entity.projectId}")

                val beforeUri = Uri.parse(entity.beforePhotoUri)
                val afterUri =
                    Uri.parse(
                        entity.afterPhotoUri
                            ?: throw IllegalStateException("After photo missing for pair: $pairId"),
                    )

                val combineConfig = combineConfigOverride ?: combineSettingsRepository.getConfig()
                val withWatermark =
                    if (watermarkConfig != null) {
                        pairImageComposer.combineSideBySideWithWatermark(
                            beforeUri,
                            afterUri,
                            combineConfig,
                            watermarkConfig,
                        )
                    } else {
                        pairImageComposer.combineSideBySide(beforeUri, afterUri, combineConfig)
                    }

                val sequenceNumber = extractSequenceNumber(entity.beforePhotoUri)
                val settings = appSettingsRepository.settingsFlow.first()
                val fileName = fileNameGenerator.generatePairFileName(sequenceNumber, settings.fileNamePrefix)

                val savedUri =
                    try {
                        mediaStoreManager.saveBitmapToGallery(withWatermark, project.name, fileName, settings.jpegQuality)
                    } finally {
                        withWatermark.recycle()
                    }

                try {
                    val uriString = savedUri.toString()
                    photoPairDao.update(
                        entity.copy(
                            combinedPhotoUri = uriString,
                            status = PairStatus.COMBINED.name,
                        ),
                    )
                    projectDao.updateTimestamp(entity.projectId, System.currentTimeMillis())
                    uriString
                } catch (e: Exception) {
                    mediaStoreManager.deleteFromGallery(savedUri)
                    throw e
                }
            }

        private fun extractSequenceNumber(beforePhotoUri: String): Int {
            val match = Regex("BEFORE_(\\d+)_").find(beforePhotoUri)
            return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
        }
    }
