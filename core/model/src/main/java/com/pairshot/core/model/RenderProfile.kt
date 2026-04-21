package com.pairshot.core.model

enum class RenderProfile(
    val borderScale: Float,
) {
    FULL(1.0f),
    PREVIEW(0.25f),
    THUMBNAIL(0.125f),
}
