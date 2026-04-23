package com.pairshot.core.model

data class CameraCapabilities(
    val hasFlash: Boolean = false,
    val nightModeAvailable: Boolean = false,
    val hdrAvailable: Boolean = false,
    val exposureIndexMin: Int = 0,
    val exposureIndexMax: Int = 0,
    val exposureStepNumerator: Int = 1,
    val exposureStepDenominator: Int = 1,
)
