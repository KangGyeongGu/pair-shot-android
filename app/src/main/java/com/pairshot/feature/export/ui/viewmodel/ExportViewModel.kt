package com.pairshot.feature.export.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.app.navigation.route.Export
import com.pairshot.core.domain.export.ExportRepository
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPairRepository
import com.pairshot.core.domain.project.ProjectRepository
import com.pairshot.core.domain.settings.WatermarkConfig
import com.pairshot.core.domain.settings.WatermarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExportFormat { INDIVIDUAL, ZIP }

sealed interface ExportUiState {
    data object Loading : ExportUiState

    data class Success(
        val projectName: String,
        val pairCount: Int,
        val beforeCount: Int,
        val afterCount: Int,
        val combinedCount: Int,
        val incompleteCount: Int,
    ) : ExportUiState

    data class Error(
        val message: String,
    ) : ExportUiState
}

sealed interface ExportAction {
    data class ShareImages(
        val uris: List<String>,
    ) : ExportAction

    data class ShareZip(
        val filePath: String,
    ) : ExportAction
}

@HiltViewModel
class ExportViewModel
    @Inject
    constructor(
        private val projectRepository: ProjectRepository,
        private val photoPairRepository: PhotoPairRepository,
        private val exportRepository: ExportRepository,
        private val watermarkRepository: WatermarkRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val route = savedStateHandle.toRoute<Export>()
        val projectId: Long = route.projectId
        val pairIds: List<Long> = route.pairIds.split(",").mapNotNull { it.toLongOrNull() }

        private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Loading)
        val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

        private val _includeBefore = MutableStateFlow(true)
        val includeBefore: StateFlow<Boolean> = _includeBefore.asStateFlow()

        private val _includeAfter = MutableStateFlow(true)
        val includeAfter: StateFlow<Boolean> = _includeAfter.asStateFlow()

        private val _includeCombined = MutableStateFlow(true)
        val includeCombined: StateFlow<Boolean> = _includeCombined.asStateFlow()

        private val _exportFormat = MutableStateFlow(ExportFormat.ZIP)
        val exportFormat: StateFlow<ExportFormat> = _exportFormat.asStateFlow()

        private val _isExporting = MutableStateFlow(false)
        val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

        private val _exportProgress = MutableStateFlow(0f)
        val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

        private val _snackbarMessage = MutableSharedFlow<String>()
        val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

        private val _applyWatermark = MutableStateFlow(false)
        val applyWatermark: StateFlow<Boolean> = _applyWatermark.asStateFlow()

        private val _exportAction = MutableSharedFlow<ExportAction>()
        val exportAction: SharedFlow<ExportAction> = _exportAction.asSharedFlow()

        init {
            loadSelectedPairsInfo()
            loadWatermarkDefault()
        }

        private fun loadWatermarkDefault() {
            viewModelScope.launch {
                val config = watermarkRepository.getConfig()
                _applyWatermark.value = config.enabled
            }
        }

        fun setApplyWatermark(value: Boolean) {
            _applyWatermark.update { value }
        }

        private fun loadSelectedPairsInfo() {
            viewModelScope.launch {
                val projectName =
                    try {
                        projectRepository.getById(projectId)?.name ?: "프로젝트"
                    } catch (_: Exception) {
                        "프로젝트"
                    }

                val pairs =
                    pairIds.mapNotNull { id ->
                        try {
                            photoPairRepository.getById(id)
                        } catch (_: Exception) {
                            null
                        }
                    }

                val afterCount = pairs.count { it.afterPhotoUri != null }
                val combinableCount =
                    pairs.count { it.status == PairStatus.PAIRED || it.status == PairStatus.COMBINED }
                val incompleteCount = pairs.count { it.status == PairStatus.BEFORE_ONLY }

                if (afterCount == 0) _includeAfter.value = false
                if (combinableCount == 0) _includeCombined.value = false

                _uiState.value =
                    ExportUiState.Success(
                        projectName = projectName,
                        pairCount = pairs.size,
                        beforeCount = pairs.count { it.beforePhotoUri.isNotBlank() },
                        afterCount = afterCount,
                        combinedCount = combinableCount,
                        incompleteCount = incompleteCount,
                    )
            }
        }

        fun setIncludeBefore(value: Boolean) {
            _includeBefore.update { value }
        }

        fun setIncludeAfter(value: Boolean) {
            _includeAfter.update { value }
        }

        fun setIncludeCombined(value: Boolean) {
            _includeCombined.update { value }
        }

        fun setExportFormat(format: ExportFormat) {
            _exportFormat.update { format }
        }

        private suspend fun ensureCombined() {
            if (!_includeCombined.value) return
            val uncombinedIds =
                pairIds.mapNotNull { id ->
                    val pair = photoPairRepository.getById(id)
                    if (pair?.status == PairStatus.PAIRED) id else null
                }
            if (uncombinedIds.isEmpty()) return
            _snackbarMessage.emit("${uncombinedIds.size}개 페어 합성 중...")
            uncombinedIds.forEach { id ->
                try {
                    photoPairRepository.combinePair(id)
                } catch (_: Exception) {
                }
            }
            loadSelectedPairsInfo()
        }

        private suspend fun resolveWatermarkConfig(): WatermarkConfig? =
            if (_applyWatermark.value) watermarkRepository.getConfig() else null

        fun saveToDevice(outputUri: String? = null) {
            viewModelScope.launch {
                if (!validateSelection()) return@launch
                _isExporting.value = true
                _exportProgress.value = 0f
                try {
                    ensureCombined()
                    val wmConfig = resolveWatermarkConfig()
                    when (_exportFormat.value) {
                        ExportFormat.ZIP -> {
                            if (outputUri == null) return@launch
                            exportRepository.exportZip(
                                pairIds = pairIds,
                                outputUri = outputUri,
                                includeBefore = _includeBefore.value,
                                includeAfter = _includeAfter.value,
                                includeCombined = _includeCombined.value,
                                watermarkConfig = wmConfig,
                                onProgress = { current, total ->
                                    _exportProgress.value =
                                        if (total > 0) current.toFloat() / total else 0f
                                },
                            )
                            _snackbarMessage.emit("ZIP 파일 저장 완료")
                        }

                        ExportFormat.INDIVIDUAL -> {
                            val state = _uiState.value as? ExportUiState.Success ?: return@launch
                            exportRepository.saveImagesToGallery(
                                pairIds = pairIds,
                                projectName = state.projectName,
                                includeBefore = _includeBefore.value,
                                includeAfter = _includeAfter.value,
                                includeCombined = _includeCombined.value,
                                watermarkConfig = wmConfig,
                                onProgress = { current, total ->
                                    _exportProgress.value =
                                        if (total > 0) current.toFloat() / total else 0f
                                },
                            )
                            _snackbarMessage.emit("갤러리에 저장 완료")
                        }
                    }
                } catch (e: Exception) {
                    _snackbarMessage.emit("저장 실패: ${e.message}")
                } finally {
                    _isExporting.value = false
                    _exportProgress.value = 0f
                }
            }
        }

        fun share() {
            viewModelScope.launch {
                if (!validateSelection()) return@launch
                _isExporting.value = true
                _exportProgress.value = 0f
                try {
                    ensureCombined()
                    val wmConfig = resolveWatermarkConfig()
                    when (_exportFormat.value) {
                        ExportFormat.ZIP -> {
                            val state = _uiState.value as? ExportUiState.Success ?: return@launch
                            val filePath =
                                exportRepository.createShareableZip(
                                    pairIds = pairIds,
                                    projectName = state.projectName,
                                    includeBefore = _includeBefore.value,
                                    includeAfter = _includeAfter.value,
                                    includeCombined = _includeCombined.value,
                                    watermarkConfig = wmConfig,
                                    onProgress = { current, total ->
                                        _exportProgress.value =
                                            if (total > 0) current.toFloat() / total else 0f
                                    },
                                )
                            _exportAction.emit(ExportAction.ShareZip(filePath))
                        }

                        ExportFormat.INDIVIDUAL -> {
                            val uris =
                                exportRepository.prepareShareableImages(
                                    pairIds = pairIds,
                                    includeBefore = _includeBefore.value,
                                    includeAfter = _includeAfter.value,
                                    includeCombined = _includeCombined.value,
                                    watermarkConfig = wmConfig,
                                    onProgress = { current, total ->
                                        _exportProgress.value =
                                            if (total > 0) current.toFloat() / total else 0f
                                    },
                                )
                            _exportAction.emit(ExportAction.ShareImages(uris))
                        }
                    }
                } catch (e: Exception) {
                    _snackbarMessage.emit("공유 준비 실패: ${e.message}")
                } finally {
                    _isExporting.value = false
                    _exportProgress.value = 0f
                }
            }
        }

        private suspend fun validateSelection(): Boolean {
            if (!_includeBefore.value && !_includeAfter.value && !_includeCombined.value) {
                _snackbarMessage.emit("최소 하나의 항목을 선택해주세요")
                return false
            }
            val state = _uiState.value as? ExportUiState.Success ?: return false
            val hasContent =
                (_includeBefore.value && state.beforeCount > 0) ||
                    (_includeAfter.value && state.afterCount > 0) ||
                    (_includeCombined.value && state.combinedCount > 0)
            if (!hasContent) {
                _snackbarMessage.emit("내보낼 수 있는 파일이 없습니다")
                return false
            }
            return true
        }
    }
