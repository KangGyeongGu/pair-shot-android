package com.pairshot.data.repository.pair

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.util.FileNameGenerator
import com.pairshot.core.util.PairImageComposer
import com.pairshot.data.local.db.dao.PhotoPairDao
import com.pairshot.data.local.db.dao.ProjectDao
import com.pairshot.data.local.db.entity.PhotoPairEntity
import com.pairshot.data.local.db.entity.toDomain
import com.pairshot.data.local.db.entity.toEntity
import com.pairshot.data.local.storage.MediaStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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
                    } catch (_: Exception) {
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
                val pairId = photoPairDao.insert(entity)
                projectDao.updateTimestamp(projectId, System.currentTimeMillis())
                pairId
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
                } catch (_: Exception) {
                }
            }
            entity.combinedPhotoUri?.let {
                try {
                    mediaStoreManager.deleteFromGallery(Uri.parse(it))
                } catch (_: Exception) {
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
        }

        override suspend fun combinePair(pairId: Long): String =
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

                val combined = pairImageComposer.combineSideBySide(beforeUri, afterUri)

                val sequenceNumber = extractSequenceNumber(entity.beforePhotoUri)
                val settings = appSettingsRepository.settingsFlow.first()
                val fileName = fileNameGenerator.generatePairFileName(sequenceNumber, settings.fileNamePrefix)

                val savedUri =
                    try {
                        mediaStoreManager.saveBitmapToGallery(combined, project.name, fileName, settings.jpegQuality)
                    } finally {
                        combined.recycle()
                    }

                val uriString = savedUri.toString()
                photoPairDao.update(
                    entity.copy(
                        combinedPhotoUri = uriString,
                        status = PairStatus.COMBINED.name,
                    ),
                )
                projectDao.updateTimestamp(entity.projectId, System.currentTimeMillis())

                uriString
            }

        private fun extractSequenceNumber(beforePhotoUri: String): Int {
            val match = Regex("BEFORE_(\\d+)_").find(beforePhotoUri)
            return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
        }
    }
