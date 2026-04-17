package com.pairshot.data.repository

import android.net.Uri
import com.pairshot.data.local.db.dao.PhotoPairDao
import com.pairshot.data.local.db.dao.ProjectDao
import com.pairshot.data.local.db.entity.PhotoPairEntity
import com.pairshot.data.local.db.entity.toDomain
import com.pairshot.data.local.db.entity.toEntity
import com.pairshot.data.local.storage.MediaStoreManager
import com.pairshot.domain.model.PairStatus
import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.repository.PhotoPairRepository
import com.pairshot.util.FileNameGenerator
import com.pairshot.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PhotoPairRepositoryImpl
    @Inject
    constructor(
        private val photoPairDao: PhotoPairDao,
        private val projectDao: ProjectDao,
        private val mediaStoreManager: MediaStoreManager,
        private val fileNameGenerator: FileNameGenerator,
        private val imageUtils: ImageUtils,
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
            }

        override suspend fun delete(pair: PhotoPair) =
            withContext(Dispatchers.IO) {
                mediaStoreManager.deleteFromGallery(Uri.parse(pair.beforePhotoUri))
                pair.afterPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                pair.combinedPhotoUri?.let { mediaStoreManager.deleteFromGallery(Uri.parse(it)) }
                photoPairDao.delete(pair.toEntity())
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
            }

        override fun countByProject(projectId: Long): Flow<Int> = photoPairDao.countByProject(projectId)

        override suspend fun getAllByProjectOnce(projectId: Long): List<PhotoPair> =
            withContext(Dispatchers.IO) {
                photoPairDao.getAllByProjectOnce(projectId).map { it.toDomain() }
            }

        override suspend fun saveBeforePhoto(
            projectId: Long,
            tempFileUri: String,
            zoomLevel: Float?,
            lensId: String?,
        ): Long =
            withContext(Dispatchers.IO) {
                // 프로젝트 이름 조회
                val project =
                    projectDao.getById(projectId)
                        ?: throw IllegalArgumentException("Project not found: $projectId")

                // 시퀀스 번호 결정
                val currentCount = photoPairDao.countByProject(projectId).first()
                val sequenceNumber = currentCount + 1

                // 파일명 생성
                val fileName = fileNameGenerator.generateBeforeFileName(sequenceNumber)

                // 1. MediaStore에 저장 (파일 저장 먼저!)
                val savedUri =
                    mediaStoreManager.saveToGallery(
                        tempFileUri = Uri.parse(tempFileUri),
                        projectName = project.name,
                        displayName = fileName,
                    )

                // 2. DB에 기록 (파일 저장 성공 후에만!)
                val entity =
                    PhotoPairEntity(
                        projectId = projectId,
                        beforePhotoUri = savedUri.toString(),
                        beforeTimestamp = System.currentTimeMillis(),
                        status = PairStatus.BEFORE_ONLY.name,
                        zoomLevel = zoomLevel,
                        lensId = lensId,
                    )
                photoPairDao.insert(entity)
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

            val sequenceNumber = extractSequenceNumber(entity.beforePhotoUri)
            val fileName = fileNameGenerator.generateAfterFileName(sequenceNumber)

            val savedUri =
                mediaStoreManager.saveToGallery(
                    tempFileUri = Uri.parse(tempFileUri),
                    projectName = project.name,
                    displayName = fileName,
                )

            photoPairDao.update(
                entity.copy(
                    afterPhotoUri = savedUri.toString(),
                    afterTimestamp = System.currentTimeMillis(),
                    status = PairStatus.PAIRED.name,
                ),
            )
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

                // combineSideBySide는 내부에서 EXIF 보정 + recycle 처리
                val combined = imageUtils.combineSideBySide(beforeUri, afterUri)

                val sequenceNumber = extractSequenceNumber(entity.beforePhotoUri)
                val fileName = fileNameGenerator.generatePairFileName(sequenceNumber)

                val savedUri =
                    try {
                        mediaStoreManager.saveBitmapToGallery(combined, project.name, fileName)
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

                uriString
            }

        private fun extractSequenceNumber(beforePhotoUri: String): Int {
            val match = Regex("BEFORE_(\\d+)_").find(beforePhotoUri)
            return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
        }
    }
