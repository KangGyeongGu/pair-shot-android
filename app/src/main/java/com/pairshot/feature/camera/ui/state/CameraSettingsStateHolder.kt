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

/**
 * 카메라 설정 상태와 로직을 캡슐화하는 순수 클래스.
 * Hilt 주입 불필요 — ViewModel 내부에서 직접 생성한다.
 *
 * ZoomStateHolder 패턴과 동일하게 ViewModel이 직접 생성하여 보유한다.
 */
class CameraSettingsStateHolder {
    private val _capabilities = MutableStateFlow(CameraCapabilities())
    val capabilities: StateFlow<CameraCapabilities> = _capabilities.asStateFlow()

    private val _settingsState = MutableStateFlow(CameraSettingsState())
    val settingsState: StateFlow<CameraSettingsState> = _settingsState.asStateFlow()

    /**
     * 카메라 바인딩 시 호출.
     * CameraInfo와 ExtensionsManager로 기기 지원 여부를 조회해 capabilities를 업데이트한다.
     * 전면 카메라 전환 시에도 재호출되어야 한다.
     */
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

        // 전면 전환 시 flash 관련 상태 초기화
        if (!cameraInfo.hasFlashUnit()) {
            _settingsState.update { it.copy(flashMode = FlashMode.OFF) }
        }
        // 전면 전환 시 미지원 Extension 자동 비활성화
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

    /**
     * 수평계 토글. 반환값(활성 여부)으로 ViewModel이 sensor start/stop을 결정한다.
     */
    fun toggleLevel(): Boolean {
        val next = !_settingsState.value.levelEnabled
        _settingsState.update { it.copy(levelEnabled = next) }
        return next
    }

    /** OFF → AUTO → ON → TORCH → OFF 순환 */
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

    /** 야간모드 토글. HDR과 동시 활성화 불가 — 야간모드를 켜면 HDR은 자동 OFF. */
    fun toggleNightMode() {
        val next = !_settingsState.value.nightModeEnabled
        _settingsState.update {
            it.copy(
                nightModeEnabled = next,
                hdrEnabled = if (next) false else it.hdrEnabled,
            )
        }
    }

    /** HDR 토글. 야간모드와 동시 활성화 불가 — HDR을 켜면 야간모드는 자동 OFF. */
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

    /**
     * 현재 활성화된 Extension에 따라 적절한 CameraSelector를 반환한다.
     * Extension이 비활성화된 경우 기본 selector를 반환한다.
     */
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

    /**
     * 현재 flashMode에 따라 ImageCapture의 flashMode를 설정한다.
     * TORCH 모드는 ImageCapture.flashMode가 아닌 CameraControl.enableTorch()로 처리하므로
     * 이 함수는 TORCH일 때 FLASH_MODE_OFF를 설정한다.
     */
    fun applyFlashMode(imageCapture: ImageCapture) {
        imageCapture.flashMode =
            when (_settingsState.value.flashMode) {
                FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
                FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
                FlashMode.ON -> ImageCapture.FLASH_MODE_ON
                FlashMode.TORCH -> ImageCapture.FLASH_MODE_OFF // Torch는 CameraControl.enableTorch()로 처리
            }
    }
}
