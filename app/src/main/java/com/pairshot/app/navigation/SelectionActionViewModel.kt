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
    data class Info(
        val text: String,
    ) : SelectionMessage

    data class Error(
        val text: String,
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
                _progress.value = Progress("공유 준비 중", 0, ids.size)
                runCatching {
                    val (preset, combine, watermark) = loadConfig()
                    val action =
                        shareSelectionUseCase(ids.toList(), preset, watermark, combine) { current, total ->
                            _progress.value = Progress("공유 준비 중", current, total)
                        }
                    _exportAction.emit(action)
                }.onFailure { error ->
                    Timber.e(error, "공유 실패")
                    _messages.emit(SelectionMessage.Error("공유 준비 실패"))
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
                        SelectionMessage.Info("선택한 ${ids.size}개 페어 모두 합성본이 이미 있습니다"),
                    )
                    return@launch
                }

                if (preset.format == ExportFormat.ZIP) {
                    _pendingZipSave.value = PendingZipSave(ids.toList(), preset, combine, watermark)
                    _messages.emit(SelectionMessage.Info("ZIP 저장 위치를 선택하세요"))
                } else {
                    runSave(ids.toList(), preset, combine, watermark, outputUri = null)
                }
            }
        }

        fun completeZipSave(outputUri: String?) {
            val pending = _pendingZipSave.value ?: return
            _pendingZipSave.value = null
            if (outputUri == null) {
                viewModelScope.launch { _messages.emit(SelectionMessage.Info("저장 취소됨")) }
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
            _progress.value = Progress("기기 저장 중", 0, pairIds.size)
            runCatching {
                saveSelectionToDeviceUseCase(pairIds, preset, watermark, combine, outputUri) { current, total ->
                    _progress.value = Progress("기기 저장 중", current, total)
                }
                val message =
                    if (skippedCombined > 0) {
                        "저장 완료 · 합성본 ${skippedCombined}개는 이미 있어 건너뜀"
                    } else {
                        "저장 완료"
                    }
                _messages.emit(SelectionMessage.Info(message))
            }.onFailure { error ->
                Timber.e(error, "기기저장 실패")
                _messages.emit(SelectionMessage.Error("저장 실패"))
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
    val label: String,
    val current: Int,
    val total: Int,
)
