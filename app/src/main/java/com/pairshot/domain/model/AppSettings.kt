package com.pairshot.domain.model

data class AppSettings(
    val jpegQuality: Int = 85,
    val fileNamePrefix: String = "PAIRSHOT",
    val overlayEnabled: Boolean = true,
    val defaultOverlayAlpha: Float = 0.3f,
)
