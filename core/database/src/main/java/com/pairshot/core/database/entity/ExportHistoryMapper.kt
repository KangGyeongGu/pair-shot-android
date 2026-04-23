package com.pairshot.core.database.entity

import com.pairshot.core.model.ExportHistoryEntry
import com.pairshot.core.model.ExportHistoryKind

fun ExportHistoryEntity.toDomain() =
    ExportHistoryEntry(
        id = id,
        pairId = pairId,
        mediaStoreUri = mediaStoreUri,
        kind = runCatching { ExportHistoryKind.valueOf(kind) }.getOrDefault(ExportHistoryKind.COMBINED),
        createdAt = createdAt,
    )

fun ExportHistoryEntry.toEntity() =
    ExportHistoryEntity(
        id = id,
        pairId = pairId,
        mediaStoreUri = mediaStoreUri,
        kind = kind.name,
        createdAt = createdAt,
    )
