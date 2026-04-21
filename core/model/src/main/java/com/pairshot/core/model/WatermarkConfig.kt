package com.pairshot.core.model

data class WatermarkConfig(
    val enabled: Boolean = false,
    val type: WatermarkType = WatermarkType.TEXT,
    val text: String = "",
    val alpha: Float = 0.3f,
    val diagonalCount: Int = 5,
    val repeatDensity: Float = 1.0f,
    val textSizeRatio: Float = 0.03f,
    val logoPath: String = "",
    val logoPosition: LogoPosition = LogoPosition.BOTTOM_RIGHT,
    val logoSizeRatio: Float = 0.5f,
    val logoAlpha: Float = 0.5f,
)
