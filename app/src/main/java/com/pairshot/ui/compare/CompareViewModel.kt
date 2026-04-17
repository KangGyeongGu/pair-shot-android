package com.pairshot.ui.compare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.domain.model.PhotoPair
import com.pairshot.domain.repository.PhotoPairRepository
import com.pairshot.domain.usecase.combine.CombineImagesUseCase
import com.pairshot.ui.navigation.Compare
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompareViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
        private val combineImagesUseCase: CombineImagesUseCase,
    ) : ViewModel() {
        private val pairId: Long = savedStateHandle.toRoute<Compare>().pairId

        private val _pair = MutableStateFlow<PhotoPair?>(null)
        val pair: StateFlow<PhotoPair?> = _pair.asStateFlow()

        private val _pairNumber = MutableStateFlow(0)
        val pairNumber: StateFlow<Int> = _pairNumber.asStateFlow()

        private val _deleteComplete = MutableSharedFlow<Unit>()
        val deleteComplete: SharedFlow<Unit> = _deleteComplete.asSharedFlow()

        private val _retakeReady = MutableSharedFlow<PhotoPair>()
        val retakeReady: SharedFlow<PhotoPair> = _retakeReady.asSharedFlow()

        private val _combineComplete = MutableSharedFlow<String>()
        val combineComplete: SharedFlow<String> = _combineComplete.asSharedFlow()

        private val _isCombining = MutableStateFlow(false)
        val isCombining: StateFlow<Boolean> = _isCombining.asStateFlow()

        init {
            loadPair()
        }

        private fun loadPair() {
            viewModelScope.launch {
                val loaded = photoPairRepository.getById(pairId)
                _pair.value = loaded
                if (loaded != null) {
                    val allPairs = photoPairRepository.getPairsByProject(loaded.projectId).first()
                    val index = allPairs.indexOfFirst { it.id == pairId }
                    _pairNumber.value = if (index >= 0) index + 1 else 0
                }
            }
        }

        fun prepareRetake() {
            viewModelScope.launch {
                _pair.value?.let { pair ->
                    photoPairRepository.resetAfterPhoto(pair.id)
                    _retakeReady.emit(pair)
                }
            }
        }

        fun combinePair() {
            viewModelScope.launch {
                _isCombining.value = true
                try {
                    combineImagesUseCase(pairId)
                    _pair.value = photoPairRepository.getById(pairId)
                    _combineComplete.emit("합성 완료")
                } catch (_: Exception) {
                    _combineComplete.emit("합성 실패")
                } finally {
                    _isCombining.value = false
                }
            }
        }

        fun deletePair() {
            viewModelScope.launch {
                _pair.value?.let { pair ->
                    photoPairRepository.delete(pair)
                    _deleteComplete.emit(Unit)
                }
            }
        }
    }
