package com.pairshot.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.domain.combine.CombineHistoryRepository
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.domain.export.ExportAction
import com.pairshot.core.domain.export.SaveSelectionToDeviceUseCase
import com.pairshot.core.domain.export.ShareSelectionUseCase
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportFormat
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

    data class Info(
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
        private val combineSettingsRepository: CombineSettingsRepository,
        private val watermarkRepository: WatermarkRepository,
        private val appSettingsRepository: AppSettingsRepository,
        private val combineHistoryRepository: CombineHistoryRepository,
    ) : ViewModel() {
        private val _exportAction = MutableSharedFlow<ExportAction>()
        val exportAction: SharedFlow<ExportAction> = _exportAction.asSharedFlow()

        private val _messages = MutableSharedFlow<SelectionMessage>()
        val messages: SharedFlow<SelectionMessage> = _messages.asSharedFlow()

        private val _progress = MutableStateFlow<Progress?>(null)
        val progress: StateFlow<Progress?> = _progress.asStateFlow()

        private val _pendingZipSave = MutableStateFlow<PendingZipSave?>(null)
        val pendingZipSave: StateFlow<PendingZipSave?> = _pendingZipSave.asStateFlow()

        fun shareSelection(ids: Set<Long>) {
            if (ids.isEmpty()) return
            viewModelScope.launch {
                _progress.value = Progress(UiText.Resource(com.pairshot.R.string.progress_sharing), 0, ids.size)
                runCatching {
                    val (preset, combine, watermark) = loadConfig()
                    val action =
                        shareSelectionUseCase(ids.toList(), preset, watermark, combine) { current, total ->
                            _progress.value = Progress(UiText.Resource(com.pairshot.R.string.progress_sharing), current, total)
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
                if (shouldSkipEntirely(ids, preset)) {
                    _messages.emit(
                        SelectionMessage.Info(UiText.Resource(R.string.snackbar_info_combined_exists)),
                    )
                    return@launch
                }

                if (preset.format == ExportFormat.ZIP) {
                    _pendingZipSave.value = PendingZipSave(ids.toList(), preset, combine, watermark)
                    _messages.emit(SelectionMessage.Info(UiText.Resource(R.string.snackbar_info_select_zip_location)))
                } else {
                    runSave(ids.toList(), preset, combine, watermark, outputUri = null)
                }
            }
        }

        fun completeZipSave(outputUri: String?) {
            val pending = _pendingZipSave.value ?: return
            _pendingZipSave.value = null
            if (outputUri == null) {
                viewModelScope.launch {
                    _messages.emit(SelectionMessage.Info(UiText.Resource(R.string.snackbar_info_save_cancelled)))
                }
                return
            }
            viewModelScope.launch {
                runSave(pending.pairIds, pending.preset, pending.combine, pending.watermark, outputUri)
            }
        }

        private suspend fun shouldSkipEntirely(
            ids: Set<Long>,
            preset: ExportPreset,
        ): Boolean {
            if (!preset.includeCombined || preset.includeBefore || preset.includeAfter) return false
            val existing = combineHistoryRepository.findByPairIds(ids.toList())
            return existing.size == ids.size
        }

        private suspend fun runSave(
            pairIds: List<Long>,
            preset: ExportPreset,
            combine: CombineConfig,
            watermark: WatermarkConfig?,
            outputUri: String?,
        ) {
            val skippedCombined =
                if (preset.includeCombined) {
                    combineHistoryRepository.findByPairIds(pairIds).size
                } else {
                    0
                }
            _progress.value = Progress(UiText.Resource(com.pairshot.R.string.progress_saving), 0, pairIds.size)
            runCatching {
                saveSelectionToDeviceUseCase(pairIds, preset, watermark, combine, outputUri) { current, total ->
                    _progress.value = Progress(UiText.Resource(com.pairshot.R.string.progress_saving), current, total)
                }
                val message =
                    if (skippedCombined > 0) {
                        UiText.Resource(R.string.snackbar_info_saved_to_device_partial)
                    } else {
                        UiText.Resource(R.string.snackbar_info_saved_to_device)
                    }
                _messages.emit(SelectionMessage.Info(message))
            }.onFailure { error ->
                Timber.e(error, "save to device failed")
                _messages.emit(SelectionMessage.Error(UiText.Resource(R.string.snackbar_error_save_failed)))
            }
            _progress.value = null
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

data class PendingZipSave(
    val pairIds: List<Long>,
    val preset: ExportPreset,
    val combine: CombineConfig,
    val watermark: WatermarkConfig?,
)

data class Progress(
    val label: UiText,
    val current: Int,
    val total: Int,
)
