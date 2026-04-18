package com.pairshot.core.domain.settings

data class AppSettings(
    val jpegQuality: Int = 85,
    val fileNamePrefix: String = "PAIRSHOT",
    val overlayEnabled: Boolean = true,
    val defaultOverlayAlpha: Float = 0.3f,
    val cameraGridEnabled: Boolean = false,
    val cameraLevelEnabled: Boolean = false,
    val cameraFlashMode: String = "OFF",
    val cameraNightModeEnabled: Boolean = false,
    val cameraHdrEnabled: Boolean = false,
)
