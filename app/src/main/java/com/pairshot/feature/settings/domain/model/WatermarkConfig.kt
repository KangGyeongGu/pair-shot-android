package com.pairshot.feature.settings.domain.model

enum class WatermarkType { TEXT, LOGO }

// 로고 위치 (9-point grid)
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
    // 텍스트 설정
    val text: String = "",
    val alpha: Float = 0.3f,
    val diagonalCount: Int = 5,
    val repeatDensity: Float = 1.0f,
    val textSizeRatio: Float = 0.03f,
    // 로고 설정
    val logoPath: String = "",
    val logoPosition: LogoPosition = LogoPosition.BOTTOM_RIGHT,
    val logoSizeRatio: Float = 0.15f,
    val logoAlpha: Float = 0.5f,
)
