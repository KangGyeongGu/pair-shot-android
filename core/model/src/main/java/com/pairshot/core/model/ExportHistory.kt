package com.pairshot.core.model

data class ExportHistoryEntry(
    val id: Long = 0,
    val pairId: Long,
    val mediaStoreUri: String,
    val kind: ExportHistoryKind,
    val createdAt: Long = 0,
)

enum class ExportHistoryKind {
    COMBINED,
    WATERMARKED_BEFORE,
    WATERMARKED_AFTER,
}
