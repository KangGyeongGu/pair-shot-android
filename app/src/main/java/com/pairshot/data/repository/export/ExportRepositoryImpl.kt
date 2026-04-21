package com.pairshot.data.repository.export

import android.net.Uri
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.domain.export.ExportRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.ExifBitmapLoader
import com.pairshot.core.database.dao.PhotoPairDao
import com.pairshot.data.local.export.ExportEntryFactory
import com.pairshot.data.local.export.ShareImagePreparer
import com.pairshot.data.local.export.WatermarkedBitmapWriter
import com.pairshot.data.local.export.WatermarkedZipTask
import com.pairshot.core.storage.MediaStoreManager
import com.pairshot.core.storage.ZipImageEntry
import com.pairshot.core.storage.ZipManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
        private val exifBitmapLoader: ExifBitmapLoader,
        private val watermarkedBitmapWriter: WatermarkedBitmapWriter,
        private val exportEntryFactory: ExportEntryFactory,
        private val shareImagePreparer: ShareImagePreparer,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ExportRepository {
        override suspend fun exportZip(
            pairIds: List<Long>,
            outputUri: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            watermarkConfig: WatermarkConfig?,
            combineConfig: CombineConfig,
            onProgress: (current: Int, total: Int) -> Unit,
        ): Unit =
            withContext(Dispatchers.IO) {
                val jpegQuality = appSettingsRepository.settingsFlow.first().jpegQuality
                val pairs = photoPairDao.getByIds(pairIds)
                if (watermarkConfig != null) {
                    val tempDir = shareImagePreparer.prepareTempDir("export_wm")
                    val tasks = exportEntryFactory.buildWatermarkedZipEntries(pairs, includeBefore, includeAfter, includeCombined, tempDir)
                    val entries = buildZipEntriesFromTasks(tasks, watermarkConfig, jpegQuality, combineConfig)
                    zipManager.createZip(entries = entries, outputUri = Uri.parse(outputUri), onProgress = onProgress)
                    tempDir.deleteRecursively()
                } else {
                    val entries = exportEntryFactory.buildZipEntries(pairs, includeBefore, includeAfter, includeCombined)
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
            combineConfig: CombineConfig,
            onProgress: (current: Int, total: Int) -> Unit,
        ): String =
            withContext(Dispatchers.IO) {
                val jpegQuality = appSettingsRepository.settingsFlow.first().jpegQuality
                val pairs = photoPairDao.getByIds(pairIds)
                val shareDir = shareImagePreparer.prepareTempDir("share_zip")
                val outputFile = File(shareDir, "PairShot_$projectName.zip")
                if (watermarkConfig != null) {
                    val tempDir = shareImagePreparer.prepareTempDir("share_wm")
                    val tasks = exportEntryFactory.buildWatermarkedZipEntries(pairs, includeBefore, includeAfter, includeCombined, tempDir)
                    val entries = buildZipEntriesFromTasks(tasks, watermarkConfig, jpegQuality, combineConfig)
                    zipManager.createZipToFile(entries = entries, outputFile = outputFile, onProgress = onProgress)
                    tempDir.deleteRecursively()
                } else {
                    val entries = exportEntryFactory.buildZipEntries(pairs, includeBefore, includeAfter, includeCombined)
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
            combineConfig: CombineConfig,
            onProgress: (current: Int, total: Int) -> Unit,
        ): List<String> =
            withContext(Dispatchers.IO) {
                val jpegQuality = appSettingsRepository.settingsFlow.first().jpegQuality
                val pairs = photoPairDao.getByIds(pairIds)
                val shareDir = shareImagePreparer.prepareShareImageDir()
                val shareEntries =
                    exportEntryFactory.buildShareEntries(
                        pairs,
                        includeBefore,
                        includeAfter,
                        includeCombined,
                        watermarkConfig != null,
                    )

                shareEntries.mapIndexed { index, entry ->
                    val destFile = File(shareDir, entry.fileName)
                    if (watermarkConfig != null && !entry.isCombined && entry.sourceUri.isNotBlank()) {
                        watermarkedBitmapWriter.applyWatermarkToFile(entry.sourceUri, destFile, watermarkConfig, jpegQuality)
                    } else if (entry.isCombined && watermarkConfig != null) {
                        watermarkedBitmapWriter.combineWithWatermark(
                            entry.beforeUri!!,
                            entry.afterUri!!,
                            destFile,
                            watermarkConfig,
                            jpegQuality,
                            combineConfig,
                        )
                    } else {
                        shareImagePreparer.copyFromContentUri(entry.sourceUri, destFile)
                    }
                    onProgress(index + 1, shareEntries.size)
                    shareImagePreparer.getFileProviderUri(destFile).toString()
                }
            }

        override suspend fun saveImagesToGallery(
            pairIds: List<Long>,
            projectName: String,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            watermarkConfig: WatermarkConfig?,
            combineConfig: CombineConfig,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            val jpegQuality = appSettingsRepository.settingsFlow.first().jpegQuality
            val pairs = photoPairDao.getByIds(pairIds)
            val galleryEntries =
                exportEntryFactory.buildGalleryEntries(
                    pairs,
                    includeBefore,
                    includeAfter,
                    includeCombined,
                    watermarkConfig != null,
                )

            val hasCombinedWm = watermarkConfig != null && galleryEntries.any { it.isCombined }
            val tempDir = if (hasCombinedWm) shareImagePreparer.prepareTempDir("gallery_wm") else null
            try {
                galleryEntries.forEachIndexed { index, entry ->
                    if (watermarkConfig != null && !entry.isCombined && entry.sourceUri.isNotBlank()) {
                        val watermarkedConfig = watermarkConfig.copy(enabled = true)
                        val tempFile = File(shareImagePreparer.prepareTempDir("gallery_single"), entry.displayName)
                        watermarkedBitmapWriter.applyWatermarkToFile(entry.sourceUri, tempFile, watermarkedConfig, jpegQuality)
                        mediaStoreManager.saveToGallery(Uri.fromFile(tempFile), projectName, entry.displayName)
                    } else if (entry.isCombined && watermarkConfig != null && tempDir != null) {
                        val tempFile = File(tempDir, entry.displayName)
                        watermarkedBitmapWriter.combineWithWatermark(
                            entry.beforeUri!!,
                            entry.afterUri!!,
                            tempFile,
                            watermarkConfig,
                            jpegQuality,
                            combineConfig,
                        )
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

        private suspend fun buildZipEntriesFromTasks(
            tasks: List<WatermarkedZipTask>,
            config: WatermarkConfig,
            jpegQuality: Int,
            combineConfig: CombineConfig = CombineConfig(),
        ): List<ZipImageEntry> {
            val enabledConfig = config.copy(enabled = true)
            return tasks.map { task ->
                if (task.isCombined) {
                    watermarkedBitmapWriter.combineWithWatermark(
                        task.sourceUri,
                        task.afterUri!!,
                        task.destFile,
                        enabledConfig,
                        jpegQuality,
                        combineConfig,
                    )
                } else {
                    watermarkedBitmapWriter.applyWatermarkToFile(task.sourceUri, task.destFile, enabledConfig, jpegQuality)
                }
                ZipImageEntry(uri = Uri.fromFile(task.destFile), entryPath = task.entryPath)
            }
        }
    }
