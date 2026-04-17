package com.pairshot.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.pairshot.data.local.db.dao.PhotoPairDao
import com.pairshot.data.local.db.entity.PhotoPairEntity
import com.pairshot.data.local.image.WatermarkManager
import com.pairshot.data.local.storage.MediaStoreManager
import com.pairshot.data.local.storage.ZipImageEntry
import com.pairshot.data.local.storage.ZipManager
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.domain.repository.ExportRepository
import com.pairshot.util.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportRepositoryImpl
    @Inject
    constructor(
        private val photoPairDao: PhotoPairDao,
        private val zipManager: ZipManager,
        private val mediaStoreManager: MediaStoreManager,
        private val watermarkManager: WatermarkManager,
        private val imageUtils: ImageUtils,
        @ApplicationContext private val context: Context,
    ) : ExportRepository {
        override suspend fun exportZip(
            pairIds: List<Long>,
            outputUri: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ): Unit =
            withContext(Dispatchers.IO) {
                val pairs = photoPairDao.getByIds(pairIds)
                if (watermarkConfig != null) {
                    val tempDir = prepareTempDir("export_wm")
                    val entries =
                        buildWatermarkedZipEntries(
                            pairs,
                            includeBefore,
                            includeAfter,
                            includeCombined,
                            watermarkConfig,
                            tempDir,
                        )
                    zipManager.createZip(entries = entries, outputUri = Uri.parse(outputUri), onProgress = onProgress)
                    tempDir.deleteRecursively()
                } else {
                    val entries = buildZipEntries(pairs, includeBefore, includeAfter, includeCombined)
                    zipManager.createZip(entries = entries, outputUri = Uri.parse(outputUri), onProgress = onProgress)
                }
            }

        override suspend fun createShareableZip(
            pairIds: List<Long>,
            projectName: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ): String =
            withContext(Dispatchers.IO) {
                val pairs = photoPairDao.getByIds(pairIds)
                val shareDir = File(context.cacheDir, "share")
                shareDir.mkdirs()
                shareDir.listFiles()?.forEach { it.delete() }
                val outputFile = File(shareDir, "PairShot_$projectName.zip")
                if (watermarkConfig != null) {
                    val tempDir = prepareTempDir("share_wm")
                    val entries =
                        buildWatermarkedZipEntries(
                            pairs,
                            includeBefore,
                            includeAfter,
                            includeCombined,
                            watermarkConfig,
                            tempDir,
                        )
                    zipManager.createZipToFile(entries = entries, outputFile = outputFile, onProgress = onProgress)
                    tempDir.deleteRecursively()
                } else {
                    val entries = buildZipEntries(pairs, includeBefore, includeAfter, includeCombined)
                    zipManager.createZipToFile(entries = entries, outputFile = outputFile, onProgress = onProgress)
                }
                outputFile.absolutePath
            }

        override suspend fun prepareShareableImages(
            pairIds: List<Long>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ): List<String> =
            withContext(Dispatchers.IO) {
                val pairs = photoPairDao.getByIds(pairIds)
                val authority = "${context.packageName}.fileprovider"

                val shareDir = File(context.cacheDir, "share_images")
                shareDir.deleteRecursively()
                shareDir.mkdirs()

                data class ShareEntry(
                    val sourceUri: String,
                    val fileName: String,
                    val isCombined: Boolean = false,
                    val beforeUri: String? = null,
                    val afterUri: String? = null,
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
                            if (includeCombined && watermarkConfig != null) {
                                // 워터마크 적용 시 before+after를 각각 워터마크 후 합성
                                if (pair.beforePhotoUri.isNotBlank() && pair.afterPhotoUri?.isNotBlank() == true) {
                                    add(
                                        ShareEntry(
                                            sourceUri = "",
                                            fileName = "PAIR_%03d.jpg".format(seq),
                                            isCombined = true,
                                            beforeUri = pair.beforePhotoUri,
                                            afterUri = pair.afterPhotoUri,
                                        ),
                                    )
                                }
                            } else if (includeCombined) {
                                pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let {
                                    add(ShareEntry(it, "PAIR_%03d.jpg".format(seq)))
                                }
                            }
                        }
                    }

                entries.mapIndexed { index, entry ->
                    val destFile = File(shareDir, entry.fileName)
                    if (watermarkConfig != null && !entry.isCombined && entry.sourceUri.isNotBlank()) {
                        applyWatermarkToFile(entry.sourceUri, destFile, watermarkConfig)
                    } else if (entry.isCombined && watermarkConfig != null) {
                        combineWithWatermark(entry.beforeUri!!, entry.afterUri!!, destFile, watermarkConfig)
                    } else {
                        context.contentResolver.openInputStream(Uri.parse(entry.sourceUri))?.use { input ->
                            destFile.outputStream().use { output -> input.copyTo(output) }
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
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            val pairs = photoPairDao.getByIds(pairIds)

            data class GalleryEntry(
                val sourceUri: String,
                val displayName: String,
                val isCombined: Boolean = false,
                val beforeUri: String? = null,
                val afterUri: String? = null,
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
                        if (includeCombined && watermarkConfig != null) {
                            if (pair.beforePhotoUri.isNotBlank() && pair.afterPhotoUri?.isNotBlank() == true) {
                                add(
                                    GalleryEntry(
                                        sourceUri = "",
                                        displayName = "PAIR_%03d.jpg".format(seq),
                                        isCombined = true,
                                        beforeUri = pair.beforePhotoUri,
                                        afterUri = pair.afterPhotoUri,
                                    ),
                                )
                            }
                        } else if (includeCombined) {
                            pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                                add(GalleryEntry(uri, "PAIR_%03d.jpg".format(seq)))
                            }
                        }
                    }
                }

            val hasCombinedWm = watermarkConfig != null && galleryEntries.any { it.isCombined }
            val tempDir = if (hasCombinedWm) prepareTempDir("gallery_wm") else null
            try {
                galleryEntries.forEachIndexed { index, entry ->
                    if (watermarkConfig != null && !entry.isCombined && entry.sourceUri.isNotBlank()) {
                        val bitmap = imageUtils.loadBitmapWithExifCorrection(Uri.parse(entry.sourceUri))
                        val watermarked = watermarkManager.apply(bitmap, watermarkConfig.copy(enabled = true))
                        mediaStoreManager.saveBitmapToGallery(watermarked, projectName, entry.displayName)
                        if (watermarked !== bitmap) bitmap.recycle()
                        watermarked.recycle()
                    } else if (entry.isCombined && watermarkConfig != null && tempDir != null) {
                        val tempFile = File(tempDir, entry.displayName)
                        combineWithWatermark(entry.beforeUri!!, entry.afterUri!!, tempFile, watermarkConfig)
                        mediaStoreManager.saveToGallery(Uri.fromFile(tempFile), projectName, entry.displayName)
                    } else {
                        mediaStoreManager.saveToGallery(
                            tempFileUri = Uri.parse(entry.sourceUri),
                            projectName = projectName,
                            displayName = entry.displayName,
                        )
                    }
                    onProgress(index + 1, galleryEntries.size)
                }
            } finally {
                tempDir?.deleteRecursively()
            }
        }

        private fun buildZipEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
        ): List<ZipImageEntry> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                        add(ZipImageEntry(uri = Uri.parse(pair.beforePhotoUri), entryPath = "before/BEFORE_%03d.jpg".format(seq)))
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            add(ZipImageEntry(uri = Uri.parse(uri), entryPath = "after/AFTER_%03d.jpg".format(seq)))
                        }
                    }
                    if (includeCombined) {
                        pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            add(ZipImageEntry(uri = Uri.parse(uri), entryPath = "combined/PAIR_%03d.jpg".format(seq)))
                        }
                    }
                }
            }

        private suspend fun buildWatermarkedZipEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            config: WatermarkConfig,
            tempDir: File,
        ): List<ZipImageEntry> =
            buildList {
                val enabledConfig = config.copy(enabled = true)
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                        val file = File(tempDir, "BEFORE_%03d.jpg".format(seq))
                        applyWatermarkToFile(pair.beforePhotoUri, file, enabledConfig)
                        add(ZipImageEntry(uri = Uri.fromFile(file), entryPath = "before/BEFORE_%03d.jpg".format(seq)))
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            val file = File(tempDir, "AFTER_%03d.jpg".format(seq))
                            applyWatermarkToFile(uri, file, enabledConfig)
                            add(ZipImageEntry(uri = Uri.fromFile(file), entryPath = "after/AFTER_%03d.jpg".format(seq)))
                        }
                    }
                    if (includeCombined) {
                        if (pair.beforePhotoUri.isNotBlank() && pair.afterPhotoUri?.isNotBlank() == true) {
                            val file = File(tempDir, "PAIR_%03d.jpg".format(seq))
                            combineWithWatermark(pair.beforePhotoUri, pair.afterPhotoUri, file, enabledConfig)
                            add(ZipImageEntry(uri = Uri.fromFile(file), entryPath = "combined/PAIR_%03d.jpg".format(seq)))
                        }
                    }
                }
            }

        private fun prepareTempDir(name: String): File {
            val dir = File(context.cacheDir, name)
            dir.deleteRecursively()
            dir.mkdirs()
            return dir
        }

        private suspend fun applyWatermarkToFile(
            sourceUri: String,
            destFile: File,
            config: WatermarkConfig,
        ) {
            val bitmap = imageUtils.loadBitmapWithExifCorrection(Uri.parse(sourceUri))
            val watermarked = watermarkManager.apply(bitmap, config.copy(enabled = true))
            FileOutputStream(destFile).use { out ->
                watermarked.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            if (watermarked !== bitmap) bitmap.recycle()
            watermarked.recycle()
        }

        private suspend fun combineWithWatermark(
            beforeUri: String,
            afterUri: String,
            destFile: File,
            config: WatermarkConfig,
        ) {
            val enabledConfig = config.copy(enabled = true)
            val beforeBitmap = imageUtils.loadBitmapWithExifCorrection(Uri.parse(beforeUri))
            val wmBefore = watermarkManager.apply(beforeBitmap, enabledConfig)

            val afterBitmap = imageUtils.loadBitmapWithExifCorrection(Uri.parse(afterUri))
            val wmAfter = watermarkManager.apply(afterBitmap, enabledConfig)

            val width = wmBefore.width + wmAfter.width
            val height = maxOf(wmBefore.height, wmAfter.height)
            val combined = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(combined)
            canvas.drawBitmap(wmBefore, 0f, 0f, null)
            canvas.drawBitmap(wmAfter, wmBefore.width.toFloat(), 0f, null)

            FileOutputStream(destFile).use { out ->
                combined.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            if (wmBefore !== beforeBitmap) beforeBitmap.recycle()
            wmBefore.recycle()
            if (wmAfter !== afterBitmap) afterBitmap.recycle()
            wmAfter.recycle()
            combined.recycle()
        }
    }
