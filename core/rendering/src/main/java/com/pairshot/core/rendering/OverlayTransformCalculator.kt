package com.pairshot.core.rendering

private const val FULL_ROTATION_DEG = 360f

object OverlayTransformCalculator {
    const val LANDSCAPE_LEFT_ROTATION = 90f
    const val LANDSCAPE_RIGHT_ROTATION = 270f

    fun calculate(
        sensorOrientation: Int,
        exifDegrees: Int,
    ): Float {
        val raw = (sensorOrientation - exifDegrees).toFloat()
        val mod = raw % FULL_ROTATION_DEG
        return (mod + FULL_ROTATION_DEG) % FULL_ROTATION_DEG
    }
}
