package com.pairshot.data.repository

import android.content.Context
import android.net.Uri
import com.pairshot.data.local.db.dao.PhotoPairDao
import com.pairshot.data.local.storage.MediaStoreManager
import com.pairshot.data.local.storage.ZipImageEntry
import com.pairshot.data.local.storage.ZipManager
import com.pairshot.domain.repository.ExportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepositoryImpl
    @Inject
    constructor(
        private val photoPairDao: PhotoPairDao,
        private val zipManager: ZipManager,
        private val mediaStoreManager: MediaStoreManager,
        @ApplicationContext private val context: Context,
    ) : ExportRepository {
        override suspend fun exportZip(
            pairIds: List<Long>,
            outputUri: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            val pairs = photoPairDao.getByIds(pairIds)
            val entries = buildZipEntries(pairs, includeBefore, includeAfter, includeCombined)
            zipManager.createZip(
                entries = entries,
                outputUri = Uri.parse(outputUri),
                onProgress = onProgress,
            )
        }

        override suspend fun createShareableZip(
            pairIds: List<Long>,
            projectName: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            onProgress: (current: Int, total: Int) -> Unit,
        ): String =
            withContext(Dispatchers.IO) {
                val pairs = photoPairDao.getByIds(pairIds)
                val entries = buildZipEntries(pairs, includeBefore, includeAfter, includeCombined)
                val shareDir = File(context.cacheDir, "share")
                shareDir.mkdirs()
                shareDir.listFiles()?.forEach { it.delete() }
                val outputFile = File(shareDir, "PairShot_$projectName.zip")
                zipManager.createZipToFile(
                    entries = entries,
                    outputFile = outputFile,
                    onProgress = onProgress,
                )
                outputFile.absolutePath
            }

        override suspend fun prepareShareableImages(
            pairIds: List<Long>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            onProgress: (current: Int, total: Int) -> Unit,
        ): List<String> =
            withContext(Dispatchers.IO) {
                val pairs = photoPairDao.getByIds(pairIds)
                val resolver = context.contentResolver
                val authority = "${context.packageName}.fileprovider"

                // cacheDir/share_images/ 정리 후 사용
                val shareDir = File(context.cacheDir, "share_images")
                shareDir.deleteRecursively()
                shareDir.mkdirs()

                // 복사할 소스 URI + 파일명 수집
                data class ShareEntry(
                    val sourceUri: String,
                    val fileName: String,
                )

                val entries =
                    buildList {
                        pairs.forEachIndexed { index, pair ->
                            val seq = index + 1
                            if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                                add(ShareEntry(pair.beforePhotoUri, "BEFORE_%03d.jpg".format(seq)))
                            }
                            if (includeAfter) {
                                pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let {
                                    add(ShareEntry(it, "AFTER_%03d.jpg".format(seq)))
                                }
                            }
                            if (includeCombined) {
                                pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let {
                                    add(ShareEntry(it, "PAIR_%03d.jpg".format(seq)))
                                }
                            }
                        }
                    }

                // cacheDir에 복사 → FileProvider URI 생성
                entries.mapIndexed { index, entry ->
                    val destFile = File(shareDir, entry.fileName)
                    resolver.openInputStream(Uri.parse(entry.sourceUri))?.use { input ->
                        destFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    onProgress(index + 1, entries.size)
                    androidx.core.content.FileProvider
                        .getUriForFile(context, authority, destFile)
                        .toString()
                }
            }

        override suspend fun saveImagesToGallery(
            pairIds: List<Long>,
            projectName: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            val pairs = photoPairDao.getByIds(pairIds)

            data class GalleryEntry(
                val sourceUri: String,
                val displayName: String,
            )

            val galleryEntries =
                buildList {
                    pairs.forEachIndexed { index, pair ->
                        val seq = index + 1
                        if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                            add(GalleryEntry(pair.beforePhotoUri, "BEFORE_%03d.jpg".format(seq)))
                        }
                        if (includeAfter) {
                            pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                                add(GalleryEntry(uri, "AFTER_%03d.jpg".format(seq)))
                            }
                        }
                        if (includeCombined) {
                            pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                                add(GalleryEntry(uri, "PAIR_%03d.jpg".format(seq)))
                            }
                        }
                    }
                }

            galleryEntries.forEachIndexed { index, entry ->
                mediaStoreManager.saveToGallery(
                    tempFileUri = Uri.parse(entry.sourceUri),
                    projectName = projectName,
                    displayName = entry.displayName,
                )
                onProgress(index + 1, galleryEntries.size)
            }
        }

        private fun buildZipEntries(
            pairs: List<com.pairshot.data.local.db.entity.PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
        ): List<ZipImageEntry> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                        add(
                            ZipImageEntry(
                                uri = Uri.parse(pair.beforePhotoUri),
                                entryPath = "before/BEFORE_%03d.jpg".format(seq),
                            ),
                        )
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            add(
                                ZipImageEntry(
                                    uri = Uri.parse(uri),
                                    entryPath = "after/AFTER_%03d.jpg".format(seq),
                                ),
                            )
                        }
                    }
                    if (includeCombined) {
                        pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            add(
                                ZipImageEntry(
                                    uri = Uri.parse(uri),
                                    entryPath = "combined/PAIR_%03d.jpg".format(seq),
                                ),
                            )
                        }
                    }
                }
            }
    }
