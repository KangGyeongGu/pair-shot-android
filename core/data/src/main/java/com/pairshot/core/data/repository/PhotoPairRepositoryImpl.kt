package com.pairshot.core.data.repository

import android.net.Uri
import com.pairshot.core.database.dao.PairAlbumCrossRefDao
import com.pairshot.core.database.dao.PhotoPairDao
import com.pairshot.core.database.entity.PairAlbumCrossRefEntity
import com.pairshot.core.database.entity.PhotoPairEntity
import com.pairshot.core.database.entity.toDomain
import com.pairshot.core.database.entity.toEntity
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.rendering.FileNameGenerator
import com.pairshot.core.storage.DeleteException
import com.pairshot.core.storage.DeleteResult
import com.pairshot.core.storage.MediaStoreManager
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
        private val photoPairDao: PhotoPairDao,
        private val pairAlbumCrossRefDao: PairAlbumCrossRefDao,
        private val mediaStoreManager: MediaStoreManager,
        private val fileNameGenerator: FileNameGenerator,
        private val appSettingsRepository: AppSettingsRepository,
    ) : PhotoPairRepository {
        override fun observeAll(): Flow<List<PhotoPair>> =
            photoPairDao.observeAllWithCounts().map { entities ->
                entities.map { it.toDomain() }
            }

        override fun observeUnpaired(): Flow<List<PhotoPair>> =
            photoPairDao.observeUnpaired().map { entities ->
                entities.map { it.toDomain() }
            }

        override fun observeUnpairedByAlbum(albumId: Long): Flow<List<PhotoPair>> =
            pairAlbumCrossRefDao.getUnpairedByAlbum(albumId).map { entities ->
                entities.map { it.toDomain() }
            }

        override suspend fun getById(id: Long): PhotoPair? =
            withContext(Dispatchers.IO) {
                photoPairDao.getById(id)?.toDomain()
            }

        override suspend fun delete(pair: PhotoPair) {
            withContext(Dispatchers.IO) {
                deleteGalleryUriOrThrow(pair.beforePhotoUri)
                pair.afterPhotoUri?.let { deleteGalleryUriOrThrow(it) }
                photoPairDao.delete(pair.toEntity())
            }
        }

        override fun countAll(): Flow<Int> = photoPairDao.countAll()

        override suspend fun saveBeforePhoto(
            tempFileUri: String,
            zoomLevel: Float?,
            albumId: Long?,
        ): Long =
            withContext(Dispatchers.IO) {
                try {
                    val sequenceNumber = (photoPairDao.getMaxId() ?: 0L).plus(1L).toInt()

                    val prefix = appSettingsRepository.getCurrent().fileNamePrefix
                    val fileName = fileNameGenerator.generateBeforeFileName(sequenceNumber, prefix)

                    val savedUri =
                        mediaStoreManager.saveToGallery(
                            tempFileUri = Uri.parse(tempFileUri),
                            subfolder = "",
                            displayName = fileName,
                        )

                    val entity =
                        PhotoPairEntity(
                            beforePhotoUri = savedUri.toString(),
                            beforeTimestamp = System.currentTimeMillis(),
                            status = PairStatus.BEFORE_ONLY.name,
                            zoomLevel = zoomLevel,
                        )
                    try {
                        val pairId = photoPairDao.insert(entity)
                        if (albumId != null) {
                            pairAlbumCrossRefDao.insert(
                                PairAlbumCrossRefEntity(
                                    pairId = pairId,
                                    albumId = albumId,
                                ),
                            )
                        }
                        pairId
                    } catch (e: Exception) {
                        deleteGalleryUriLogOnFailure(savedUri.toString(), "BEFORE rollback failed")
                        throw e
                    }
                } finally {
                    deleteTempFile(tempFileUri)
                }
            }

        override suspend fun saveAfterPhoto(
            pairId: Long,
            tempFileUri: String,
        ) = withContext(Dispatchers.IO) {
            try {
                val entity =
                    photoPairDao.getById(pairId)
                        ?: throw IllegalArgumentException("PhotoPair not found: $pairId")

                entity.afterPhotoUri?.let { deleteGalleryUriLogOnFailure(it, "previous After photo delete failed") }

                val sequenceNumber = extractSequenceNumber(entity.beforePhotoUri)
                val prefix = appSettingsRepository.getCurrent().fileNamePrefix
                val fileName = fileNameGenerator.generateAfterFileName(sequenceNumber, prefix)

                val savedUri =
                    mediaStoreManager.saveToGallery(
                        tempFileUri = Uri.parse(tempFileUri),
                        subfolder = "",
                        displayName = fileName,
                    )

                try {
                    val now = System.currentTimeMillis()
                    photoPairDao.update(
                        entity.copy(
                            afterPhotoUri = savedUri.toString(),
                            afterTimestamp = now,
                            status = PairStatus.PAIRED.name,
                        ),
                    )
                } catch (e: Exception) {
                    deleteGalleryUriLogOnFailure(savedUri.toString(), "AFTER rollback failed")
                    throw e
                }
            } finally {
                deleteTempFile(tempFileUri)
            }
        }

        private fun deleteGalleryUriOrThrow(uriString: String) {
            val result = mediaStoreManager.deleteFromGallery(Uri.parse(uriString))
            when (result) {
                is DeleteResult.Success, DeleteResult.NotFound -> {
                    Unit
                }

                is DeleteResult.Failed -> {
                    Timber.w(result.exception, "MediaStore delete failed: $uriString")
                    throw DeleteException(result)
                }

                is DeleteResult.RecoverablePermission -> {
                    Timber.w(result.exception, "MediaStore delete permission required: $uriString")
                    throw DeleteException(result)
                }
            }
        }

        private fun deleteGalleryUriLogOnFailure(
            uriString: String,
            failureLogMessage: String,
        ) {
            val result = mediaStoreManager.deleteFromGallery(Uri.parse(uriString))
            when (result) {
                is DeleteResult.Success, DeleteResult.NotFound -> {
                    Unit
                }

                is DeleteResult.Failed -> {
                    Timber.w(result.exception, "$failureLogMessage: $uriString")
                }

                is DeleteResult.RecoverablePermission -> {
                    Timber.w(result.exception, "$failureLogMessage (permission required): $uriString")
                }
            }
        }

        private fun extractSequenceNumber(beforePhotoUri: String): Int {
            val match = Regex("BEFORE_(\\d+)_").find(beforePhotoUri)
            return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
        }

        private fun deleteTempFile(tempFileUri: String) {
            try {
                val uri = Uri.parse(tempFileUri)
                val path =
                    when (uri.scheme) {
                        "file" -> uri.path
                        null -> tempFileUri
                        else -> null
                    }
                if (path != null) java.io.File(path).delete()
            } catch (e: Exception) {
                Timber.d(e, "temp file delete failed: $tempFileUri")
            }
        }
    }
