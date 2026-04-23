package com.pairshot.core.model

enum class RenderProfile(
    val borderScale: Float,
    val maxOutputPx: Int,
) {
    FULL(borderScale = 1.0f, maxOutputPx = 0),
    PREVIEW(borderScale = 0.25f, maxOutputPx = 2560),
}
