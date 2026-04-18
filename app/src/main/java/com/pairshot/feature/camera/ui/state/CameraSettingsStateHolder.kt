package com.pairshot.feature.camera.ui.state

import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CameraSettingsStateHolder(
    initialState: CameraSettingsState = CameraSettingsState(),
) {
    private val _capabilities = MutableStateFlow(CameraCapabilities())
    val capabilities: StateFlow<CameraCapabilities> = _capabilities.asStateFlow()

    private val _settingsState = MutableStateFlow(initialState)
    val settingsState: StateFlow<CameraSettingsState> = _settingsState.asStateFlow()

    fun updateCapabilities(
        cameraInfo: CameraInfo,
        extensionsManager: ExtensionsManager,
        lensFacing: Int,
    ) {
        val baseSelector =
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

        val nightAvailable = extensionsManager.isExtensionAvailable(baseSelector, ExtensionMode.NIGHT)
        val hdrAvailable = extensionsManager.isExtensionAvailable(baseSelector, ExtensionMode.HDR)
        val exposureState = cameraInfo.exposureState

        _capabilities.update {
            CameraCapabilities(
                hasFlash = cameraInfo.hasFlashUnit(),
                nightModeAvailable = nightAvailable,
                hdrAvailable = hdrAvailable,
                exposureRange = exposureState.exposureCompensationRange,
                exposureStep = exposureState.exposureCompensationStep,
            )
        }

        if (!cameraInfo.hasFlashUnit()) {
            _settingsState.update { it.copy(flashMode = FlashMode.OFF) }
        }
        if (!nightAvailable) {
            _settingsState.update { it.copy(nightModeEnabled = false) }
        }
        if (!hdrAvailable) {
            _settingsState.update { it.copy(hdrEnabled = false) }
        }
    }

    fun toggleGrid() {
        _settingsState.update { it.copy(gridEnabled = !it.gridEnabled) }
    }

    fun toggleLevel(): Boolean {
        val next = !_settingsState.value.levelEnabled
        _settingsState.update { it.copy(levelEnabled = next) }
        return next
    }

    fun cycleFlash() {
        _settingsState.update {
            val next =
                when (it.flashMode) {
                    FlashMode.OFF -> FlashMode.AUTO
                    FlashMode.AUTO -> FlashMode.ON
                    FlashMode.ON -> FlashMode.TORCH
                    FlashMode.TORCH -> FlashMode.OFF
                }
            it.copy(flashMode = next)
        }
    }

    fun toggleNightMode() {
        val next = !_settingsState.value.nightModeEnabled
        _settingsState.update {
            it.copy(
                nightModeEnabled = next,
                hdrEnabled = if (next) false else it.hdrEnabled,
            )
        }
    }

    fun toggleHdr() {
        val next = !_settingsState.value.hdrEnabled
        _settingsState.update {
            it.copy(
                hdrEnabled = next,
                nightModeEnabled = if (next) false else it.nightModeEnabled,
            )
        }
    }

    fun setExposureIndex(index: Int) {
        _settingsState.update { it.copy(exposureIndex = index) }
    }

    fun toggleSettingsPanel() {
        _settingsState.update { it.copy(showPanel = !it.showPanel) }
    }

    fun dismissSettingsPanel() {
        _settingsState.update { it.copy(showPanel = false) }
    }

    fun getExtensionCameraSelector(
        extensionsManager: ExtensionsManager,
        lensFacing: Int,
    ): CameraSelector {
        val baseSelector =
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

        val state = _settingsState.value
        return when {
            state.nightModeEnabled && _capabilities.value.nightModeAvailable -> {
                extensionsManager.getExtensionEnabledCameraSelector(baseSelector, ExtensionMode.NIGHT)
            }

            state.hdrEnabled && _capabilities.value.hdrAvailable -> {
                extensionsManager.getExtensionEnabledCameraSelector(baseSelector, ExtensionMode.HDR)
            }

            else -> {
                baseSelector
            }
        }
    }

    fun applyFlashMode(imageCapture: ImageCapture) {
        imageCapture.flashMode =
            when (_settingsState.value.flashMode) {
                FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                FlashMode.TORCH -> ImageCapture.FLASH_MODE_OFF
            }
    }
}
