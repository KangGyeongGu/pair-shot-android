package com.pairshot.ui.camera

import android.util.Range
import android.util.Rational

data class CameraCapabilities(
    val hasFlash: Boolean = false,
    val nightModeAvailable: Boolean = false,
    val hdrAvailable: Boolean = false,
    val exposureRange: Range<Int> = Range(0, 0),
    val exposureStep: Rational = Rational(1, 1),
)

data class CameraSettingsState(
    val flashMode: FlashMode = FlashMode.OFF,
    val gridEnabled: Boolean = false,
    val levelEnabled: Boolean = false,
    val nightModeEnabled: Boolean = false,
    val hdrEnabled: Boolean = false,
    val exposureIndex: Int = 0,
    val showPanel: Boolean = false,
)

enum class FlashMode { OFF, AUTO, ON, TORCH }
