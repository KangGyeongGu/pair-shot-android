package com.pairshot.data.local.export

import android.net.Uri
import com.pairshot.core.database.entity.PhotoPairEntity
import com.pairshot.core.storage.ZipImageEntry
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DB에서 읽은 URI 문자열을 content:// URI로 안전하게 파싱한다.
 * file:// 등 다른 스킴은 API 24+에서 FileUriExposedException을 유발하므로 null을 반환해 skip한다.
 */
private fun String.toContentUriOrNull(): Uri? {
    val uri = Uri.parse(this)
    return if (uri.scheme == "content") uri else null
}

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
                        pair.beforePhotoUri.toContentUriOrNull()?.let { uri ->
                            add(ZipImageEntry(uri = uri, entryPath = "before/BEFORE_%03d.jpg".format(seq)))
                        }
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri?.takeIf { it.isNotBlank() }?.let { raw ->
                            raw.toContentUriOrNull()?.let { uri ->
                                add(ZipImageEntry(uri = uri, entryPath = "after/AFTER_%03d.jpg".format(seq)))
                            }
                        }
                    }
                    if (includeCombined) {
                        pair.combinedPhotoUri?.takeIf { it.isNotBlank() }?.let { raw ->
                            raw.toContentUriOrNull()?.let { uri ->
                                add(ZipImageEntry(uri = uri, entryPath = "combined/PAIR_%03d.jpg".format(seq)))
                            }
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
                    if (includeBefore && pair.beforePhotoUri.isNotBlank() &&
                        pair.beforePhotoUri.toContentUriOrNull() != null
                    ) {
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
                        pair.afterPhotoUri
                            ?.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }
                            ?.let { uri ->
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
                    if (includeCombined &&
                        pair.beforePhotoUri.isNotBlank() &&
                        pair.beforePhotoUri.toContentUriOrNull() != null &&
                        !pair.afterPhotoUri.isNullOrBlank() &&
                        pair.afterPhotoUri!!.toContentUriOrNull() != null
                    ) {
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
                    if (includeBefore && pair.beforePhotoUri.isNotBlank() &&
                        pair.beforePhotoUri.toContentUriOrNull() != null
                    ) {
                        add(ShareEntry(sourceUri = pair.beforePhotoUri, fileName = "BEFORE_%03d.jpg".format(seq)))
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri
                            ?.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }
                            ?.let {
                                add(ShareEntry(sourceUri = it, fileName = "AFTER_%03d.jpg".format(seq)))
                            }
                    }
                    if (includeCombined && withWatermark) {
                        if (pair.beforePhotoUri.isNotBlank() &&
                            pair.beforePhotoUri.toContentUriOrNull() != null &&
                            !pair.afterPhotoUri.isNullOrBlank() &&
                            pair.afterPhotoUri!!.toContentUriOrNull() != null
                        ) {
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
                        pair.combinedPhotoUri
                            ?.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }
                            ?.let {
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
                    if (includeBefore && pair.beforePhotoUri.isNotBlank() &&
                        pair.beforePhotoUri.toContentUriOrNull() != null
                    ) {
                        add(GalleryEntry(sourceUri = pair.beforePhotoUri, displayName = "BEFORE_%03d.jpg".format(seq)))
                    }
                    if (includeAfter) {
                        pair.afterPhotoUri
                            ?.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }
                            ?.let { uri ->
                                add(GalleryEntry(sourceUri = uri, displayName = "AFTER_%03d.jpg".format(seq)))
                            }
                    }
                    if (includeCombined && withWatermark) {
                        if (pair.beforePhotoUri.isNotBlank() &&
                            pair.beforePhotoUri.toContentUriOrNull() != null &&
                            !pair.afterPhotoUri.isNullOrBlank() &&
                            pair.afterPhotoUri!!.toContentUriOrNull() != null
                        ) {
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
                        pair.combinedPhotoUri
                            ?.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }
                            ?.let { uri ->
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
