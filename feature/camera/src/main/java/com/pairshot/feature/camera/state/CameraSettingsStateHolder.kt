package com.pairshot.feature.camera.state

import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.model.CameraCapabilities
import com.pairshot.core.model.FlashMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CameraSettingsStateHolder
    @Inject
    constructor(
        private val appSettingsRepository: AppSettingsRepository,
    ) {
        private val _state = MutableStateFlow(CameraSettingsState())
        val state: StateFlow<CameraSettingsState> = _state.asStateFlow()

        suspend fun loadInitial(): InitialCameraSessionConfig {
            val settings = appSettingsRepository.getCurrent()
            val initial =
                CameraSettingsState(
                    gridEnabled = settings.cameraGridEnabled,
                    levelEnabled = settings.cameraLevelEnabled,
                    flashMode =
                        runCatching { FlashMode.valueOf(settings.cameraFlashMode) }
                            .getOrDefault(FlashMode.OFF),
                    nightModeEnabled = settings.cameraNightModeEnabled,
                    hdrEnabled = settings.cameraHdrEnabled,
                )
            _state.value = initial
            return InitialCameraSessionConfig(
                flashMode = initial.flashMode,
                nightModeEnabled = initial.nightModeEnabled,
                hdrEnabled = initial.hdrEnabled,
            )
        }

        fun toggleGrid(scope: CoroutineScope) {
            _state.update { it.copy(gridEnabled = !it.gridEnabled) }
            persist(scope)
        }

        fun toggleLevel(scope: CoroutineScope) {
            _state.update { it.copy(levelEnabled = !it.levelEnabled) }
            persist(scope)
        }

        fun cycleFlash(scope: CoroutineScope): FlashMode {
            _state.update {
                val next =
                    when (it.flashMode) {
                        FlashMode.OFF -> FlashMode.AUTO
                        FlashMode.AUTO -> FlashMode.ON
                        FlashMode.ON -> FlashMode.TORCH
                        FlashMode.TORCH -> FlashMode.OFF
                    }
                it.copy(flashMode = next)
            }
            persist(scope)
            return _state.value.flashMode
        }

        fun toggleNightMode(scope: CoroutineScope): Boolean {
            val next = !_state.value.nightModeEnabled
            _state.update {
                it.copy(
                    nightModeEnabled = next,
                    hdrEnabled = if (next) false else it.hdrEnabled,
                )
            }
            persist(scope)
            return next
        }

        fun toggleHdr(scope: CoroutineScope): Boolean {
            val next = !_state.value.hdrEnabled
            _state.update {
                it.copy(
                    hdrEnabled = next,
                    nightModeEnabled = if (next) false else it.nightModeEnabled,
                )
            }
            persist(scope)
            return next
        }

        fun setExposureIndex(index: Int) {
            _state.update { it.copy(exposureIndex = index) }
        }

        fun toggleSettingsPanel() {
            _state.update { it.copy(showPanel = !it.showPanel) }
        }

        fun dismissSettingsPanel() {
            _state.update { it.copy(showPanel = false) }
        }

        fun adjustForCapabilities(caps: CameraCapabilities): CapabilityAdjustment {
            val current = _state.value
            var changed = current
            var adjustedFlash: FlashMode? = null
            var adjustedNight: Boolean? = null
            var adjustedHdr: Boolean? = null
            if (!caps.hasFlash && current.flashMode != FlashMode.OFF) {
                changed = changed.copy(flashMode = FlashMode.OFF)
                adjustedFlash = FlashMode.OFF
            }
            if (!caps.nightModeAvailable && current.nightModeEnabled) {
                changed = changed.copy(nightModeEnabled = false)
                adjustedNight = false
            }
            if (!caps.hdrAvailable && current.hdrEnabled) {
                changed = changed.copy(hdrEnabled = false)
                adjustedHdr = false
            }
            if (changed !== current) _state.value = changed
            return CapabilityAdjustment(adjustedFlash, adjustedNight, adjustedHdr)
        }

        private fun persist(scope: CoroutineScope) {
            val snapshot = _state.value
            scope.launch {
                appSettingsRepository.updateCameraGridEnabled(snapshot.gridEnabled)
                appSettingsRepository.updateCameraLevelEnabled(snapshot.levelEnabled)
                appSettingsRepository.updateCameraFlashMode(snapshot.flashMode.name)
                appSettingsRepository.updateCameraNightMode(snapshot.nightModeEnabled)
                appSettingsRepository.updateCameraHdr(snapshot.hdrEnabled)
            }
        }
    }
