package com.pairshot.core.model

data class ExportPreset(
    val format: String = "ZIP",
    val includeBefore: Boolean = true,
    val includeAfter: Boolean = true,
    val includeCombined: Boolean = true,
)
