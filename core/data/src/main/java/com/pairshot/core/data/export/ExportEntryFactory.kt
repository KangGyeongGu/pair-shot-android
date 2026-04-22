package com.pairshot.core.data.export

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

private fun PhotoPairEntity.validBeforeUri(): String? = beforePhotoUri.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }

private fun PhotoPairEntity.validAfterUri(): String? = afterPhotoUri?.takeIf { it.isNotBlank() && it.toContentUriOrNull() != null }

/**
 * Export 목적별로 페어당 어떤 항목(before/after/combined)을 어떤 소스로 처리할지 결정한다.
 * - includeCombined && 이미 합성본 존재: ExistingCombined (복사만, 재렌더링 금지)
 * - includeCombined && 합성본 없음 + save 경로: skip (duplicate 방지)
 * - includeCombined && 합성본 없음 + share 경로: Compose (임시 생성)
 */
internal fun resolvePairSources(
    pair: PhotoPairEntity,
    includeBefore: Boolean,
    includeAfter: Boolean,
    includeCombined: Boolean,
    existingCombined: Map<Long, String>,
    allowComposeWhenMissing: Boolean,
): List<PairSource> =
    buildList {
        if (includeBefore) pair.validBeforeUri()?.let { add(PairSource.Before(it)) }
        if (includeAfter) pair.validAfterUri()?.let { add(PairSource.After(it)) }
        if (includeCombined) {
            val before = pair.validBeforeUri()
            val after = pair.validAfterUri()
            val existingUri = existingCombined[pair.id]
            when {
                existingUri != null -> {
                    add(PairSource.ExistingCombined(existingUri))
                }

                allowComposeWhenMissing && before != null && after != null -> {
                    add(PairSource.Compose(before, after))
                }

                else -> {
                    Unit
                }
            }
        }
    }

internal sealed interface PairSource {
    data class Before(
        val uri: String,
    ) : PairSource

    data class After(
        val uri: String,
    ) : PairSource

    data class Compose(
        val beforeUri: String,
        val afterUri: String,
    ) : PairSource

    data class ExistingCombined(
        val uri: String,
    ) : PairSource
}

@Singleton
class ExportEntryFactory
    @Inject
    constructor() {
        fun buildWatermarkedZipEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            tempDir: File,
            existingCombined: Map<Long, String> = emptyMap(),
            allowComposeWhenMissingCombined: Boolean = true,
        ): List<WatermarkedZipTask> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    val sources =
                        resolvePairSources(
                            pair = pair,
                            includeBefore = includeBefore,
                            includeAfter = includeAfter,
                            includeCombined = includeCombined,
                            existingCombined = existingCombined,
                            allowComposeWhenMissing = allowComposeWhenMissingCombined,
                        )
                    sources.forEach { source ->
                        val (fileName, entryPath, task) =
                            when (source) {
                                is PairSource.Before -> {
                                    val name = "BEFORE_%03d.jpg".format(seq)
                                    Triple(
                                        name,
                                        "before/$name",
                                        WatermarkedZipTask(
                                            sourceUri = source.uri,
                                            destFile = File(tempDir, name),
                                            entryPath = "before/$name",
                                            kind = EntryKind.BEFORE,
                                            pairId = pair.id,
                                        ),
                                    )
                                }

                                is PairSource.After -> {
                                    val name = "AFTER_%03d.jpg".format(seq)
                                    Triple(
                                        name,
                                        "after/$name",
                                        WatermarkedZipTask(
                                            sourceUri = source.uri,
                                            destFile = File(tempDir, name),
                                            entryPath = "after/$name",
                                            kind = EntryKind.AFTER,
                                            pairId = pair.id,
                                        ),
                                    )
                                }

                                is PairSource.Compose -> {
                                    val name = "PAIR_%03d.jpg".format(seq)
                                    Triple(
                                        name,
                                        "combined/$name",
                                        WatermarkedZipTask(
                                            sourceUri = source.beforeUri,
                                            afterUri = source.afterUri,
                                            destFile = File(tempDir, name),
                                            entryPath = "combined/$name",
                                            kind = EntryKind.COMBINED_NEW,
                                            pairId = pair.id,
                                        ),
                                    )
                                }

                                is PairSource.ExistingCombined -> {
                                    val name = "PAIR_%03d.jpg".format(seq)
                                    Triple(
                                        name,
                                        "combined/$name",
                                        WatermarkedZipTask(
                                            sourceUri = source.uri,
                                            destFile = File(tempDir, name),
                                            entryPath = "combined/$name",
                                            kind = EntryKind.COMBINED_EXISTING,
                                            pairId = pair.id,
                                        ),
                                    )
                                }
                            }
                        add(task)
                    }
                }
            }

        fun buildShareEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            existingCombined: Map<Long, String> = emptyMap(),
        ): List<ShareEntry> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    val sources =
                        resolvePairSources(
                            pair = pair,
                            includeBefore = includeBefore,
                            includeAfter = includeAfter,
                            includeCombined = includeCombined,
                            existingCombined = existingCombined,
                            allowComposeWhenMissing = true,
                        )
                    sources.forEach { source ->
                        when (source) {
                            is PairSource.Before -> {
                                add(
                                    ShareEntry(
                                        fileName = "BEFORE_%03d.jpg".format(seq),
                                        sourceUri = source.uri,
                                        kind = EntryKind.BEFORE,
                                        pairId = pair.id,
                                    ),
                                )
                            }

                            is PairSource.After -> {
                                add(
                                    ShareEntry(
                                        fileName = "AFTER_%03d.jpg".format(seq),
                                        sourceUri = source.uri,
                                        kind = EntryKind.AFTER,
                                        pairId = pair.id,
                                    ),
                                )
                            }

                            is PairSource.Compose -> {
                                add(
                                    ShareEntry(
                                        fileName = "PAIR_%03d.jpg".format(seq),
                                        sourceUri = "",
                                        beforeUri = source.beforeUri,
                                        afterUri = source.afterUri,
                                        kind = EntryKind.COMBINED_NEW,
                                        pairId = pair.id,
                                    ),
                                )
                            }

                            is PairSource.ExistingCombined -> {
                                add(
                                    ShareEntry(
                                        fileName = "PAIR_%03d.jpg".format(seq),
                                        sourceUri = source.uri,
                                        kind = EntryKind.COMBINED_EXISTING,
                                        pairId = pair.id,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

        fun buildGalleryEntries(
            pairs: List<PhotoPairEntity>,
            includeBefore: Boolean,
            includeAfter: Boolean,
            includeCombined: Boolean,
            existingCombined: Map<Long, String> = emptyMap(),
        ): List<GalleryEntry> =
            buildList {
                pairs.forEachIndexed { index, pair ->
                    val seq = index + 1
                    val sources =
                        resolvePairSources(
                            pair = pair,
                            includeBefore = includeBefore,
                            includeAfter = includeAfter,
                            includeCombined = includeCombined,
                            existingCombined = existingCombined,
                            allowComposeWhenMissing = true,
                        )
                    sources.forEach { source ->
                        when (source) {
                            is PairSource.Before -> {
                                add(
                                    GalleryEntry(
                                        displayName = "BEFORE_%03d.jpg".format(seq),
                                        sourceUri = source.uri,
                                        kind = EntryKind.BEFORE,
                                        pairId = pair.id,
                                    ),
                                )
                            }

                            is PairSource.After -> {
                                add(
                                    GalleryEntry(
                                        displayName = "AFTER_%03d.jpg".format(seq),
                                        sourceUri = source.uri,
                                        kind = EntryKind.AFTER,
                                        pairId = pair.id,
                                    ),
                                )
                            }

                            is PairSource.Compose -> {
                                add(
                                    GalleryEntry(
                                        displayName = "PAIR_%03d.jpg".format(seq),
                                        sourceUri = "",
                                        beforeUri = source.beforeUri,
                                        afterUri = source.afterUri,
                                        kind = EntryKind.COMBINED_NEW,
                                        pairId = pair.id,
                                    ),
                                )
                            }

                            is PairSource.ExistingCombined -> {
                                Unit
                            }
                        }
                    }
                }
            }
    }

enum class EntryKind { BEFORE, AFTER, COMBINED_NEW, COMBINED_EXISTING }

data class WatermarkedZipTask(
    val sourceUri: String,
    val afterUri: String? = null,
    val destFile: File,
    val entryPath: String,
    val kind: EntryKind,
    val pairId: Long = 0,
) {
    val isCombinedNew: Boolean get() = kind == EntryKind.COMBINED_NEW
    val isCombinedExisting: Boolean get() = kind == EntryKind.COMBINED_EXISTING
}

data class ShareEntry(
    val fileName: String,
    val sourceUri: String,
    val beforeUri: String? = null,
    val afterUri: String? = null,
    val kind: EntryKind,
    val pairId: Long = 0,
) {
    val isCombinedNew: Boolean get() = kind == EntryKind.COMBINED_NEW
    val isCombinedExisting: Boolean get() = kind == EntryKind.COMBINED_EXISTING
}

data class GalleryEntry(
    val displayName: String,
    val sourceUri: String,
    val beforeUri: String? = null,
    val afterUri: String? = null,
    val kind: EntryKind,
    val pairId: Long = 0,
) {
    val isCombinedNew: Boolean get() = kind == EntryKind.COMBINED_NEW
}
