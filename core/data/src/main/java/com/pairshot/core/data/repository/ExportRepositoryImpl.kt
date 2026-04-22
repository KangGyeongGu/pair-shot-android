package com.pairshot.core.data.repository

import android.net.Uri
import com.pairshot.core.data.export.EntryKind
import com.pairshot.core.data.export.ExportEntryFactory
import com.pairshot.core.data.export.ShareImagePreparer
import com.pairshot.core.data.export.WatermarkedBitmapWriter
import com.pairshot.core.data.export.WatermarkedZipTask
import com.pairshot.core.database.dao.PhotoPairDao
import com.pairshot.core.domain.combine.CombineHistoryRepository
import com.pairshot.core.domain.export.ExportRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineHistory
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.rendering.PairImageComposer
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
        private val watermarkedBitmapWriter: WatermarkedBitmapWriter,
        private val exportEntryFactory: ExportEntryFactory,
        private val shareImagePreparer: ShareImagePreparer,
        private val pairImageComposer: PairImageComposer,
        private val appSettingsRepository: AppSettingsRepository,
        private val combineHistoryRepository: CombineHistoryRepository,
    ) : ExportRepository {
        override suspend fun exportZipToDevice(
            pairIds: List<Long>,
            outputUri: String,
            preset: ExportPreset,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ): Unit =
            withContext(Dispatchers.IO) {
                val jpegQuality = appSettingsRepository.getCurrent().jpegQuality
                val pairs = photoPairDao.getByIds(pairIds)
                // Save 경로: 이미 합성본 있는 페어는 합성본 생성 자체를 건너뛴다 (1-per-pair 룰)
                val existingCombined = existingCombinedUriMap(pairs.map { it.id })
                val tempDir = shareImagePreparer.prepareTempDir("export_wm")
                try {
                    val tasks =
                        exportEntryFactory.buildWatermarkedZipEntries(
                            pairs = pairs,
                            includeBefore = preset.includeBefore,
                            includeAfter = preset.includeAfter,
                            includeCombined = preset.includeCombined,
                            tempDir = tempDir,
                            existingCombined = existingCombined,
                            allowComposeWhenMissingCombined = true,
                        )
                    val entries =
                        buildZipEntriesFromTasks(
                            tasks,
                            watermarkConfig,
                            jpegQuality,
                            combineConfig,
                        )
                    onProgress(0, pairs.size)
                    zipManager.createZip(
                        entries = entries,
                        outputUri = Uri.parse(outputUri),
                        onProgress = { _, _ -> },
                    )

                    // 새로 합성한 것만 MediaStore에 저장 + DB upsert (기존 합성본은 그대로 유지)
                    tasks.filter { it.isCombinedNew }.forEach { task ->
                        val savedUri =
                            mediaStoreManager.saveToGallery(
                                tempFileUri = Uri.fromFile(task.destFile),
                                subfolder = "Combined",
                                displayName = task.destFile.name,
                            )
                        combineHistoryRepository.upsert(
                            CombineHistory(pairId = task.pairId, mediaStoreUri = savedUri.toString()),
                        )
                    }
                    onProgress(pairs.size, pairs.size)
                } finally {
                    tempDir.deleteRecursively()
                }
            }

        override suspend fun saveImagesToGallery(
            pairIds: List<Long>,
            preset: ExportPreset,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            val jpegQuality = appSettingsRepository.getCurrent().jpegQuality
            val pairs = photoPairDao.getByIds(pairIds)
            val existingCombined = existingCombinedUriMap(pairs.map { it.id })
            val galleryEntries =
                exportEntryFactory.buildGalleryEntries(
                    pairs = pairs,
                    includeBefore = preset.includeBefore,
                    includeAfter = preset.includeAfter,
                    includeCombined = preset.includeCombined,
                    existingCombined = existingCombined,
                )

            val hasCombined = galleryEntries.any { it.isCombinedNew }
            val tempDir = if (hasCombined) shareImagePreparer.prepareTempDir("gallery_wm") else null

            try {
                // 페어 단위로 진행도 리포트
                pairs.forEachIndexed { pairIdx, pair ->
                    val pairEntries = galleryEntries.filter { it.pairId == pair.id }
                    pairEntries.forEachIndexed { entryIdx, entry ->
                        val savedUri: Uri =
                            when {
                                entry.isCombinedNew && tempDir != null -> {
                                    val tempFile = File(tempDir, entry.displayName)
                                    if (watermarkConfig != null) {
                                        watermarkedBitmapWriter.combineWithWatermark(
                                            entry.beforeUri!!,
                                            entry.afterUri!!,
                                            tempFile,
                                            watermarkConfig,
                                            jpegQuality,
                                            combineConfig,
                                        )
                                    } else {
                                        pairImageComposer.composeToFile(
                                            beforeUri = Uri.parse(entry.beforeUri!!),
                                            afterUri = Uri.parse(entry.afterUri!!),
                                            destFile = tempFile,
                                            combineConfig = combineConfig,
                                            watermarkConfig = WatermarkConfig(),
                                            jpegQuality = jpegQuality,
                                        )
                                    }
                                    mediaStoreManager.saveToGallery(
                                        tempFileUri = Uri.fromFile(tempFile),
                                        subfolder = "Combined",
                                        displayName = entry.displayName,
                                    )
                                }

                                watermarkConfig != null && entry.sourceUri.isNotBlank() -> {
                                    val singleTempDir =
                                        shareImagePreparer.prepareTempDir("gallery_single_${pair.id}_$entryIdx")
                                    val tempFile = File(singleTempDir, entry.displayName)
                                    watermarkedBitmapWriter.applyWatermarkToFile(
                                        entry.sourceUri,
                                        tempFile,
                                        watermarkConfig,
                                        jpegQuality,
                                    )
                                    val uri =
                                        mediaStoreManager.saveToGallery(
                                            tempFileUri = Uri.fromFile(tempFile),
                                            subfolder = "",
                                            displayName = entry.displayName,
                                        )
                                    singleTempDir.deleteRecursively()
                                    uri
                                }

                                else -> {
                                    mediaStoreManager.saveToGallery(
                                        tempFileUri = Uri.parse(entry.sourceUri),
                                        subfolder = "",
                                        displayName = entry.displayName,
                                    )
                                }
                            }

                        if (entry.isCombinedNew && entry.pairId != 0L) {
                            combineHistoryRepository.upsert(
                                CombineHistory(pairId = entry.pairId, mediaStoreUri = savedUri.toString()),
                            )
                        }
                    }
                    onProgress(pairIdx + 1, pairs.size)
                }
            } finally {
                tempDir?.deleteRecursively()
            }
        }

        override suspend fun prepareShareableImages(
            pairIds: List<Long>,
            preset: ExportPreset,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ): List<String> =
            withContext(Dispatchers.IO) {
                val jpegQuality = appSettingsRepository.getCurrent().jpegQuality
                val pairs = photoPairDao.getByIds(pairIds)
                val existingCombined = existingCombinedUriMap(pairs.map { it.id })
                val shareDir = shareImagePreparer.prepareShareImageDir()
                val shareEntries =
                    exportEntryFactory.buildShareEntries(
                        pairs = pairs,
                        includeBefore = preset.includeBefore,
                        includeAfter = preset.includeAfter,
                        includeCombined = preset.includeCombined,
                        existingCombined = existingCombined,
                    )

                val results = ArrayList<String>(shareEntries.size)
                pairs.forEachIndexed { pairIdx, pair ->
                    val pairEntries = shareEntries.filter { it.pairId == pair.id }
                    pairEntries.forEach { entry ->
                        val destFile = File(shareDir, entry.fileName)
                        when {
                            entry.isCombinedNew -> {
                                if (watermarkConfig != null) {
                                    watermarkedBitmapWriter.combineWithWatermark(
                                        entry.beforeUri!!,
                                        entry.afterUri!!,
                                        destFile,
                                        watermarkConfig,
                                        jpegQuality,
                                        combineConfig,
                                    )
                                } else {
                                    pairImageComposer.composeToFile(
                                        beforeUri = Uri.parse(entry.beforeUri!!),
                                        afterUri = Uri.parse(entry.afterUri!!),
                                        destFile = destFile,
                                        combineConfig = combineConfig,
                                        watermarkConfig = WatermarkConfig(),
                                        jpegQuality = jpegQuality,
                                    )
                                }
                            }

                            entry.isCombinedExisting -> {
                                // 이미 저장 시점 워터마크가 baked-in. 재적용하지 않고 그대로 복사.
                                shareImagePreparer.copyFromContentUri(entry.sourceUri, destFile)
                            }

                            watermarkConfig != null && entry.sourceUri.isNotBlank() -> {
                                watermarkedBitmapWriter.applyWatermarkToFile(
                                    entry.sourceUri,
                                    destFile,
                                    watermarkConfig,
                                    jpegQuality,
                                )
                            }

                            else -> {
                                shareImagePreparer.copyFromContentUri(entry.sourceUri, destFile)
                            }
                        }
                        results.add(shareImagePreparer.getFileProviderUri(destFile).toString())
                    }
                    onProgress(pairIdx + 1, pairs.size)
                }
                results
            }

        override suspend fun createShareableZip(
            pairIds: List<Long>,
            preset: ExportPreset,
            combineConfig: CombineConfig,
            watermarkConfig: WatermarkConfig?,
            onProgress: (current: Int, total: Int) -> Unit,
        ): String =
            withContext(Dispatchers.IO) {
                val jpegQuality = appSettingsRepository.getCurrent().jpegQuality
                val pairs = photoPairDao.getByIds(pairIds)
                val existingCombined = existingCombinedUriMap(pairs.map { it.id })
                val shareDir = shareImagePreparer.prepareTempDir("share_zip")
                val outputFile = File(shareDir, "PairShot.zip")
                val tempDir = shareImagePreparer.prepareTempDir("share_wm")
                try {
                    val tasks =
                        exportEntryFactory.buildWatermarkedZipEntries(
                            pairs = pairs,
                            includeBefore = preset.includeBefore,
                            includeAfter = preset.includeAfter,
                            includeCombined = preset.includeCombined,
                            tempDir = tempDir,
                            existingCombined = existingCombined,
                            allowComposeWhenMissingCombined = true,
                        )
                    val entries =
                        buildZipEntriesFromTasks(
                            tasks,
                            watermarkConfig,
                            jpegQuality,
                            combineConfig,
                        )
                    onProgress(0, pairs.size)
                    zipManager.createZipToFile(
                        entries = entries,
                        outputFile = outputFile,
                        onProgress = { _, _ -> },
                    )
                    onProgress(pairs.size, pairs.size)
                } finally {
                    tempDir.deleteRecursively()
                }
                outputFile.absolutePath
            }

        private suspend fun buildZipEntriesFromTasks(
            tasks: List<WatermarkedZipTask>,
            watermarkConfig: WatermarkConfig?,
            jpegQuality: Int,
            combineConfig: CombineConfig,
        ): List<ZipImageEntry> =
            tasks.map { task ->
                when (task.kind) {
                    EntryKind.COMBINED_NEW -> {
                        if (watermarkConfig != null) {
                            watermarkedBitmapWriter.combineWithWatermark(
                                task.sourceUri,
                                task.afterUri!!,
                                task.destFile,
                                watermarkConfig,
                                jpegQuality,
                                combineConfig,
                            )
                        } else {
                            pairImageComposer.composeToFile(
                                beforeUri = Uri.parse(task.sourceUri),
                                afterUri = Uri.parse(task.afterUri!!),
                                destFile = task.destFile,
                                combineConfig = combineConfig,
                                watermarkConfig = WatermarkConfig(),
                                jpegQuality = jpegQuality,
                            )
                        }
                    }

                    EntryKind.COMBINED_EXISTING -> {
                        // 이미 워터마크 baked-in 상태 — 그대로 복사
                        shareImagePreparer.copyFromContentUri(task.sourceUri, task.destFile)
                    }

                    EntryKind.BEFORE, EntryKind.AFTER -> {
                        if (watermarkConfig != null) {
                            watermarkedBitmapWriter.applyWatermarkToFile(
                                task.sourceUri,
                                task.destFile,
                                watermarkConfig,
                                jpegQuality,
                            )
                        } else {
                            shareImagePreparer.copyFromContentUri(task.sourceUri, task.destFile)
                        }
                    }
                }
                ZipImageEntry(uri = Uri.fromFile(task.destFile), entryPath = task.entryPath)
            }

        private suspend fun existingCombinedUriMap(pairIds: List<Long>): Map<Long, String> =
            combineHistoryRepository
                .findByPairIds(pairIds)
                .mapValues { it.value.mediaStoreUri }
    }
