package com.pairshot.feature.pairpreview.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.domain.combine.DeleteCombinedPhotosUseCase
import com.pairshot.core.domain.combine.ExportHistoryRepository
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.ExportHistoryKind
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.navigation.PairPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L

data class LivePreviewInputs(
    val pair: PhotoPair,
    val config: CombineConfig,
    val watermark: WatermarkConfig,
)

sealed interface PairPreviewUiState {
    data object Loading : PairPreviewUiState

    data class Ready(
        val pair: PhotoPair,
        val hasCombined: Boolean,
        val showDeleteDialog: Boolean,
        val livePreviewInputs: LivePreviewInputs?,
    ) : PairPreviewUiState
}

@HiltViewModel
class PairPreviewViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
        private val exportHistoryRepository: ExportHistoryRepository,
        private val deleteCombinedPhotosUseCase: DeleteCombinedPhotosUseCase,
        combineSettingsRepository: CombineSettingsRepository,
        watermarkRepository: WatermarkRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<PairPreview>()
        val pairId: Long = route.pairId

        private val _pair = MutableStateFlow<PhotoPair?>(null)
        private val _hasCombined = MutableStateFlow(false)
        private val _showDeleteDialog = MutableStateFlow(false)

        private val configFlow: StateFlow<CombineConfig> =
            combineSettingsRepository.configFlow.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS),
                CombineConfig(),
            )

        private val watermarkFlow: StateFlow<WatermarkConfig> =
            watermarkRepository.watermarkConfigFlow.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS),
                WatermarkConfig(),
            )

        val uiState: StateFlow<PairPreviewUiState> =
            combine(
                _pair,
                _hasCombined,
                _showDeleteDialog,
                configFlow,
                watermarkFlow,
            ) { pair, hasCombined, showDialog, config, watermark ->
                if (pair == null) {
                    PairPreviewUiState.Loading
                } else {
                    PairPreviewUiState.Ready(
                        pair = pair,
                        hasCombined = hasCombined,
                        showDeleteDialog = showDialog,
                        livePreviewInputs =
                            if (pair.afterPhotoUri == null) {
                                null
                            } else {
                                LivePreviewInputs(pair, config, watermark)
                            },
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS),
                initialValue = PairPreviewUiState.Loading,
            )

        private val _deleteComplete = MutableSharedFlow<Unit>()
        val deleteComplete: SharedFlow<Unit> = _deleteComplete.asSharedFlow()

        init {
            loadPair()
            refreshHasCombined()
        }

        private fun loadPair() {
            viewModelScope.launch {
                _pair.value = photoPairRepository.getById(pairId)
            }
        }

        private fun refreshHasCombined() {
            viewModelScope.launch {
                _hasCombined.value =
                    exportHistoryRepository
                        .findByPairIdsAndKind(listOf(pairId), ExportHistoryKind.COMBINED)
                        .isNotEmpty()
            }
        }

        fun showDeleteDialog() {
            _showDeleteDialog.value = true
        }

        fun dismissDeleteDialog() {
            _showDeleteDialog.value = false
        }

        fun deletePair() {
            viewModelScope.launch {
                val currentPair = _pair.value ?: return@launch
                runCatching { exportHistoryRepository.deleteByPairIds(listOf(currentPair.id)) }
                    .onFailure { error ->
                        Timber.w(error, "failed to clear export history for pair ${currentPair.id}")
                    }
                photoPairRepository.delete(currentPair)
                _showDeleteDialog.value = false
                _deleteComplete.emit(Unit)
            }
        }

        fun deleteCombinedOnly() {
            viewModelScope.launch {
                deleteCombinedPhotosUseCase(listOf(pairId))
                _hasCombined.value = false
                _showDeleteDialog.value = false
            }
        }
    }
