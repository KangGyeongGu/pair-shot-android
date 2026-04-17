package com.pairshot.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.BuildConfig
import com.pairshot.domain.usecase.storage.ClearCacheUseCase
import com.pairshot.domain.usecase.storage.GetStorageInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val usedStorageBytes: Long,
        val cacheBytes: Long,
        val appVersion: String,
    ) : SettingsUiState

    data object Error : SettingsUiState
}

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val getStorageInfoUseCase: GetStorageInfoUseCase,
        private val clearCacheUseCase: ClearCacheUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        private val _snackbarMessage = MutableSharedFlow<String>()
        val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

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
                    _uiState.value =
                        SettingsUiState.Success(
                            usedStorageBytes = info.usedBytes,
                            cacheBytes = info.cacheBytes,
                            appVersion = BuildConfig.VERSION_NAME,
                        )
                } catch (_: Exception) {
                    _uiState.value = SettingsUiState.Error
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
    }

internal fun formatBytes(bytes: Long): String =
    when {
        bytes >= 1_073_741_824L -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576L -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1_024L -> "%.1f KB".format(bytes / 1_024.0)
        else -> "$bytes B"
    }
