package com.pairshot.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.domain.export.ExportAction
import com.pairshot.core.domain.export.HasSavableSelectionUseCase
import com.pairshot.core.domain.export.SaveSelectionToDeviceUseCase
import com.pairshot.core.domain.export.ShareSelectionUseCase
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportPreset
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.ui.R
import com.pairshot.core.ui.text.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface SelectionMessage {
    val text: UiText

    data class Success(
        override val text: UiText,
    ) : SelectionMessage

    data class Warning(
        override val text: UiText,
    ) : SelectionMessage

    data class Error(
        override val text: UiText,
    ) : SelectionMessage
}

@HiltViewModel
class SelectionActionViewModel
    @Inject
    constructor(
        private val shareSelectionUseCase: ShareSelectionUseCase,
        private val saveSelectionToDeviceUseCase: SaveSelectionToDeviceUseCase,
        private val hasSavableSelectionUseCase: HasSavableSelectionUseCase,
        private val combineSettingsRepository: CombineSettingsRepository,
        private val watermarkRepository: WatermarkRepository,
        private val appSettingsRepository: AppSettingsRepository,
    ) : ViewModel() {
        private val _exportAction = MutableSharedFlow<ExportAction>()
        val exportAction: SharedFlow<ExportAction> = _exportAction.asSharedFlow()

        private val _messages = MutableSharedFlow<SelectionMessage>()
        val messages: SharedFlow<SelectionMessage> = _messages.asSharedFlow()

        private val _progress = MutableStateFlow<Progress?>(null)
        val progress: StateFlow<Progress?> = _progress.asStateFlow()

        fun shareSelection(ids: Set<Long>) {
            if (ids.isEmpty()) return
            viewModelScope.launch {
                val shareLabel = UiText.Resource(com.pairshot.R.string.progress_sharing)
                _progress.value = Progress(shareLabel, 0, ids.size)
                runCatching {
                    val (preset, combine, watermark) = loadConfig()
                    val action =
                        shareSelectionUseCase(
                            pairIds = ids.toList(),
                            preset = preset,
                            watermarkConfig = watermark,
                            combineConfig = combine,
                        ) { current, total ->
                            _progress.value = Progress(shareLabel, current, total)
                        }
                    _exportAction.emit(action)
                }.onFailure { error ->
                    Timber.e(error, "share failed")
                    _messages.emit(SelectionMessage.Error(UiText.Resource(R.string.snackbar_error_share_failed)))
                }
                _progress.value = null
            }
        }

        fun saveSelectionToDevice(ids: Set<Long>) {
            if (ids.isEmpty()) return
            viewModelScope.launch {
                val (preset, combine, watermark) = loadConfig()
                val hasWork =
                    runCatching {
                        hasSavableSelectionUseCase(ids.toList(), preset, watermark)
                    }.getOrDefault(false)
                if (!hasWork) {
                    _messages.emit(
                        SelectionMessage.Warning(UiText.Resource(R.string.snackbar_warning_nothing_to_save)),
                    )
                    return@launch
                }
                val saveLabel = UiText.Resource(com.pairshot.R.string.progress_saving)
                _progress.value = Progress(saveLabel, 0, ids.size)
                runCatching {
                    saveSelectionToDeviceUseCase(
                        pairIds = ids.toList(),
                        preset = preset,
                        watermarkConfig = watermark,
                        combineConfig = combine,
                    ) { current, total ->
                        _progress.value = Progress(saveLabel, current, total)
                    }
                }.onSuccess { result ->
                    _messages.emit(buildSaveMessage(result))
                }.onFailure { error ->
                    Timber.e(error, "save to device failed")
                    _messages.emit(SelectionMessage.Error(UiText.Resource(R.string.snackbar_error_save_failed)))
                }
                _progress.value = null
            }
        }

        private fun buildSaveMessage(savedCount: Int): SelectionMessage =
            if (savedCount > 0) {
                SelectionMessage.Success(UiText.Resource(R.string.snackbar_success_saved_to_device))
            } else {
                SelectionMessage.Warning(UiText.Resource(R.string.snackbar_warning_nothing_to_save))
            }

        private suspend fun loadConfig(): Triple<ExportPreset, CombineConfig, WatermarkConfig?> {
            val preset = appSettingsRepository.getLastExportPreset()
            val combine = combineSettingsRepository.configFlow.first()
            val watermark =
                runCatching { watermarkRepository.watermarkConfigFlow.first() }
                    .getOrNull()
                    ?.takeIf { it.enabled }
            return Triple(preset, combine, watermark)
        }
    }

data class Progress(
    val label: UiText,
    val current: Int,
    val total: Int,
)
