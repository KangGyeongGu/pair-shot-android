package com.pairshot.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Home

@Serializable
data class AlbumDetail(
    val albumId: Long,
)

@Serializable
data class PairPicker(
    val albumId: Long,
)

@Serializable
data class PairPreview(
    val pairId: Long,
)

@Serializable
data class ExportSettings(
    val pairIds: String,
)

@Serializable
data class Camera(
    val albumId: Long? = null,
)

@Serializable
data class AfterCamera(
    val initialPairId: Long? = null,
    val albumId: Long? = null,
)

@Serializable
data object Settings

@Serializable
data object WatermarkSettings

@Serializable
data object CombineSettings

@Serializable
data object License
