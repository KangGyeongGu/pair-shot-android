package com.pairshot.core.domain.repository

import com.pairshot.core.domain.model.WatermarkConfig

interface ExportRepository {
    suspend fun exportZip(
        pairIds: List<Long>,
        outputUri: String,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    )

    suspend fun createShareableZip(
        pairIds: List<Long>,
        projectName: String,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    ): String

    suspend fun prepareShareableImages(
        pairIds: List<Long>,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    ): List<String>

    suspend fun saveImagesToGallery(
        pairIds: List<Long>,
        projectName: String,
        includeBefore: Boolean,
        includeAfter: Boolean,
        includeCombined: Boolean,
        watermarkConfig: WatermarkConfig? = null,
        onProgress: (current: Int, total: Int) -> Unit,
    )
}
