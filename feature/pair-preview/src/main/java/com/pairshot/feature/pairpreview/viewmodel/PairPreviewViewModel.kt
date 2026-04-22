package com.pairshot.feature.pairpreview.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.domain.combine.CombineHistoryRepository
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.domain.combine.DeleteCombinedPhotosUseCase
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.model.CombineHistory
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LivePreviewInputs(
    val pair: PhotoPair,
    val config: CombineConfig,
    val watermark: WatermarkConfig,
)

@HiltViewModel
class PairPreviewViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
        private val combineHistoryRepository: CombineHistoryRepository,
        private val deleteCombinedPhotosUseCase: DeleteCombinedPhotosUseCase,
        combineSettingsRepository: CombineSettingsRepository,
        watermarkRepository: WatermarkRepository,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<PairPreview>()
        val pairId: Long = route.pairId

        private val _pair = MutableStateFlow<PhotoPair?>(null)
        val pair: StateFlow<PhotoPair?> = _pair.asStateFlow()

        private val _combined = MutableStateFlow<CombineHistory?>(null)
        val combined: StateFlow<CombineHistory?> = _combined.asStateFlow()

        val currentConfig: StateFlow<CombineConfig> =
            combineSettingsRepository.configFlow.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                CombineConfig(),
            )

        val currentWatermark: StateFlow<WatermarkConfig> =
            watermarkRepository.watermarkConfigFlow.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                WatermarkConfig(),
            )

        val livePreviewInputs: StateFlow<LivePreviewInputs?> =
            combine(
                _pair,
                currentConfig,
                currentWatermark,
                _combined,
            ) { pair, config, watermark, combined ->
                if (pair == null || pair.afterPhotoUri == null || combined != null) {
                    null
                } else {
                    LivePreviewInputs(pair, config, watermark)
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        private val _showDeleteDialog = MutableStateFlow(false)
        val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

        private val _deleteComplete = MutableSharedFlow<Unit>()
        val deleteComplete: SharedFlow<Unit> = _deleteComplete.asSharedFlow()

        init {
            loadPair()
            loadCombined()
        }

        private fun loadPair() {
            viewModelScope.launch {
                _pair.value = photoPairRepository.getById(pairId)
            }
        }

        private fun loadCombined() {
            viewModelScope.launch {
                _combined.value = combineHistoryRepository.getByPair(pairId)
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
                runCatching { deleteCombinedPhotosUseCase(listOf(currentPair.id)) }
                photoPairRepository.delete(currentPair)
                _showDeleteDialog.value = false
                _deleteComplete.emit(Unit)
            }
        }

        fun deleteCombinedOnly() {
            viewModelScope.launch {
                deleteCombinedPhotosUseCase(listOf(pairId))
                _combined.value = null
                _showDeleteDialog.value = false
            }
        }
    }
