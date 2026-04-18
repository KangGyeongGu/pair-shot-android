package com.pairshot.core.domain.settings

data class ExportPreset(
    val format: String = "ZIP",
    val includeBefore: Boolean = true,
    val includeAfter: Boolean = true,
    val includeCombined: Boolean = true,
)
