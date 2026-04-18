package com.pairshot.feature.export.data.service

import android.net.Uri
import com.pairshot.data.local.db.entity.PhotoPairEntity
import com.pairshot.data.local.storage.ZipImageEntry
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportEntryFactory
    @Inject
    constructor() {
        fun buildZipEntries(
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

        fun buildWatermarkedZipEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            tempDir: File,
        ): List<WatermarkedZipTask> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                        val file = File(tempDir, "BEFORE_%03d.jpg".format(seq))
                        add(
                            WatermarkedZipTask(
                                sourceUri = pair.beforePhotoUri,
                                destFile = file,
                                entryPath = "before/BEFORE_%03d.jpg".format(seq),
                                isCombined = false,
                            ),
                        )
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            val file = File(tempDir, "AFTER_%03d.jpg".format(seq))
                            add(
                                WatermarkedZipTask(
                                    sourceUri = uri,
                                    destFile = file,
                                    entryPath = "after/AFTER_%03d.jpg".format(seq),
                                    isCombined = false,
                                ),
                            )
                        }
                    }
                    if (includeCombined && pair.beforePhotoUri.isNotBlank() && pair.afterPhotoUri?.isNotBlank() == true) {
                        val file = File(tempDir, "PAIR_%03d.jpg".format(seq))
                        add(
                            WatermarkedZipTask(
                                sourceUri = pair.beforePhotoUri,
                                afterUri = pair.afterPhotoUri,
                                destFile = file,
                                entryPath = "combined/PAIR_%03d.jpg".format(seq),
                                isCombined = true,
                            ),
                        )
                    }
                }
            }

        fun buildShareEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            withWatermark: Boolean,
        ): List<ShareEntry> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                        add(ShareEntry(sourceUri = pair.beforePhotoUri, fileName = "BEFORE_%03d.jpg".format(seq)))
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let {
                            add(ShareEntry(sourceUri = it, fileName = "AFTER_%03d.jpg".format(seq)))
                        }
                    }
                    if (includeCombined && withWatermark) {
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
                            add(ShareEntry(sourceUri = it, fileName = "PAIR_%03d.jpg".format(seq)))
                        }
                    }
                }
            }

        fun buildGalleryEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            withWatermark: Boolean,
        ): List<GalleryEntry> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    if (includeBefore && pair.beforePhotoUri.isNotBlank()) {
                        add(GalleryEntry(sourceUri = pair.beforePhotoUri, displayName = "BEFORE_%03d.jpg".format(seq)))
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { uri ->
                            add(GalleryEntry(sourceUri = uri, displayName = "AFTER_%03d.jpg".format(seq)))
                        }
                    }
                    if (includeCombined && withWatermark) {
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
                            add(GalleryEntry(sourceUri = uri, displayName = "PAIR_%03d.jpg".format(seq)))
                        }
                    }
                }
            }
    }

data class WatermarkedZipTask(
    val sourceUri: String,
    val afterUri: String? = null,
    val destFile: File,
    val entryPath: String,
    val isCombined: Boolean,
)

data class ShareEntry(
    val sourceUri: String,
    val fileName: String,
    val isCombined: Boolean = false,
    val beforeUri: String? = null,
    val afterUri: String? = null,
)

data class GalleryEntry(
    val sourceUri: String,
    val displayName: String,
    val isCombined: Boolean = false,
    val beforeUri: String? = null,
    val afterUri: String? = null,
)
