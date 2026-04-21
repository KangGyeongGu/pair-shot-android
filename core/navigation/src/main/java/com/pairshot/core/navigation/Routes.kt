package com.pairshot.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ProjectList

@Serializable
data class ProjectDetail(
    val projectId: Long,
)

@Serializable
data class Camera(
    val projectId: Long,
)

@Serializable
data class AfterCamera(
    val projectId: Long,
    val initialPairId: Long? = null,
)

@Serializable
data class Compare(
    val pairId: Long,
)

@Serializable
data class Export(
    val projectId: Long,
    val pairIds: String,
)

@Serializable
data object Settings

@Serializable
data object WatermarkSettings

@Serializable
data object CombineSettings

@Serializable
data class CombinedViewer(
    val pairId: Long,
)

@Serializable
data object License
