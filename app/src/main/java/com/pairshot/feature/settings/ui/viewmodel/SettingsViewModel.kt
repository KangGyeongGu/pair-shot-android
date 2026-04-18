package com.pairshot.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.BuildConfig
import com.pairshot.data.local.image.WatermarkManager
import com.pairshot.domain.model.WatermarkConfig
import com.pairshot.domain.repository.AppSettingsRepository
import com.pairshot.domain.repository.WatermarkRepository
import com.pairshot.domain.usecase.storage.ClearCacheUseCase
import com.pairshot.domain.usecase.storage.GetStorageInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val usedStorageBytes: Long,
        val cacheBytes: Long,
        val appVersion: String,
        val jpegQuality: Int = 85,
        val fileNamePrefix: String = "PAIRSHOT",
        val overlayEnabled: Boolean = true,
        val overlayAlpha: Float = 0.3f,
    ) : SettingsUiState

    data object Error : SettingsUiState
}

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val getStorageInfoUseCase: GetStorageInfoUseCase,
        private val clearCacheUseCase: ClearCacheUseCase,
        private val watermarkRepository: WatermarkRepository,
        val watermarkManager: WatermarkManager,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val _storageState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)

        val uiState: StateFlow<SettingsUiState> =
            combine(
                _storageState,
                appSettingsRepository.settingsFlow,
            ) { storageState, appSettings ->
                when (storageState) {
                    is SettingsUiState.Success -> {
                        storageState.copy(
                            jpegQuality = appSettings.jpegQuality,
                            fileNamePrefix = appSettings.fileNamePrefix,
                            overlayEnabled = appSettings.overlayEnabled,
                            overlayAlpha = appSettings.defaultOverlayAlpha,
                        )
                    }

                    else -> {
                        storageState
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState.Loading,
            )

        private val _snackbarMessage = MutableSharedFlow<String>()
        val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

        val watermarkConfig: StateFlow<WatermarkConfig> =
            watermarkRepository.watermarkConfigFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WatermarkConfig(),
            )

        init {
            loadStorageInfo()
        }

        fun refresh() {
            loadStorageInfo()
        }

        private fun loadStorageInfo() {
            viewModelScope.launch {
                try {
                    val info = getStorageInfoUseCase()
                    _storageState.update {
                        SettingsUiState.Success(
                            usedStorageBytes = info.usedBytes,
                            cacheBytes = info.cacheBytes,
                            appVersion = BuildConfig.VERSION_NAME,
                        )
                    }
                } catch (_: Exception) {
                    _storageState.value = SettingsUiState.Error
                }
            }
        }

        fun showMessage(message: String) {
            viewModelScope.launch {
                _snackbarMessage.emit(message)
            }
        }

        fun clearCache() {
            viewModelScope.launch {
                val freed = clearCacheUseCase()
                _snackbarMessage.emit("캐시 ${formatBytes(freed)} 정리됨")
                loadStorageInfo()
            }
        }

        fun updateWatermarkConfig(config: WatermarkConfig) {
            viewModelScope.launch {
                watermarkRepository.saveConfig(config)
            }
        }

        fun saveLogoFile(uri: String) {
            viewModelScope.launch {
                try {
                    val path = watermarkRepository.saveLogoFile(uri)
                    val current = watermarkConfig.value
                    watermarkRepository.saveConfig(current.copy(logoPath = path))
                } catch (_: Exception) {
                    _snackbarMessage.emit("로고 파일을 불러올 수 없습니다")
                }
            }
        }

        fun updateJpegQuality(quality: Int) {
            viewModelScope.launch {
                appSettingsRepository.updateJpegQuality(quality)
            }
        }

        fun updateFileNamePrefix(prefix: String) {
            viewModelScope.launch {
                appSettingsRepository.updateFileNamePrefix(prefix)
            }
        }

        fun updateOverlayEnabled(enabled: Boolean) {
            viewModelScope.launch {
                appSettingsRepository.updateOverlayEnabled(enabled)
            }
        }

        fun updateOverlayAlpha(alpha: Float) {
            viewModelScope.launch {
                appSettingsRepository.updateOverlayAlpha(alpha)
            }
        }
    }

internal fun formatBytes(bytes: Long): String =
    when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
