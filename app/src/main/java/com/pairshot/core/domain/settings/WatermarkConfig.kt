package com.pairshot.core.domain.settings

enum class WatermarkType { TEXT, LOGO }

enum class LogoPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
}

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
