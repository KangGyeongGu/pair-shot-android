package com.pairshot.core.rendering

object OverlayTransformCalculator {
    fun calculate(
        sensorOrientation: Int,
        exifDegrees: Int,
    ): Float {
        val raw = (sensorOrientation - exifDegrees).toFloat()
        val mod = raw % 360f
        return (mod + 360f) % 360f
    }
}
