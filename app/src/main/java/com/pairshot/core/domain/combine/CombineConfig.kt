package com.pairshot.core.domain.combine

data class CombineConfig(
    val layout: CombineLayout = CombineLayout.HORIZONTAL,
    val borderEnabled: Boolean = false,
    val borderThicknessDp: Int = 12,
    val borderColorArgb: Int = 0xFFFFFFFF.toInt(),
    val labelEnabled: Boolean = true,
    val beforeLabel: String = "BEFORE",
    val afterLabel: String = "AFTER",
    val labelPositionMode: LabelPositionMode = LabelPositionMode.FULL_WIDTH,
    val labelPosition: LabelPosition = LabelPosition.BOTTOM,
    val beforeLabelAnchor: LabelAnchor = LabelAnchor.BOTTOM_LEFT,
    val afterLabelAnchor: LabelAnchor = LabelAnchor.BOTTOM_LEFT,
    val labelBgEnabled: Boolean = true,
    val labelBgCornerDp: Int = 0,
    val labelSizeRatio: Float = 0.04f,
    val labelTextColorArgb: Int = 0xFFFFFFFF.toInt(),
    val labelBgColorArgb: Int = 0xFF000000.toInt(),
    val labelBgAlpha: Float = 0.45f,
    val labelBgMatchesBorder: Boolean = true,
)

enum class CombineLayout { HORIZONTAL, VERTICAL }

enum class LabelPosition { TOP, BOTTOM }

enum class LabelPositionMode { FULL_WIDTH, FREE }

enum class LabelAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    MIDDLE_LEFT,
    MIDDLE_CENTER,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
}
