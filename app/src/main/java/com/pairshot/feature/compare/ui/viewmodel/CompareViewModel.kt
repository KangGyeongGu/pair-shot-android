package com.pairshot.feature.compare.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.app.navigation.route.Compare
import com.pairshot.core.domain.pair.CombineImagesUseCase
import com.pairshot.core.domain.pair.PairStatus
import com.pairshot.core.domain.pair.PhotoPair
import com.pairshot.core.domain.pair.PhotoPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
        private val initialPairId: Long = savedStateHandle.toRoute<Compare>().pairId

        private val _pair = MutableStateFlow<PhotoPair?>(null)
        val pair: StateFlow<PhotoPair?> = _pair.asStateFlow()

        private val _pairNumber = MutableStateFlow(0)
        val pairNumber: StateFlow<Int> = _pairNumber.asStateFlow()

        private val _pairs = MutableStateFlow<List<PhotoPair>>(emptyList())
        val pairs: StateFlow<List<PhotoPair>> = _pairs.asStateFlow()

        private val _currentPairId = MutableStateFlow(initialPairId)
        val currentPairId: StateFlow<Long> = _currentPairId.asStateFlow()

        private val _deleteComplete = MutableSharedFlow<Unit>()
        val deleteComplete: SharedFlow<Unit> = _deleteComplete.asSharedFlow()

        private val _retakeReady = MutableSharedFlow<PhotoPair>()
        val retakeReady: SharedFlow<PhotoPair> = _retakeReady.asSharedFlow()

        private val _combineComplete = MutableSharedFlow<String>()
        val combineComplete: SharedFlow<String> = _combineComplete.asSharedFlow()

        private val _isCombining = MutableStateFlow(false)
        val isCombining: StateFlow<Boolean> = _isCombining.asStateFlow()

        init {
            observePairs()
        }

        private fun observePairs() {
            viewModelScope.launch {
                val loaded = photoPairRepository.getById(initialPairId) ?: return@launch
                _currentPairId.value = loaded.id

                photoPairRepository.getPairsByProject(loaded.projectId).collect { allPairs ->
                    _pairs.value = allPairs
                    syncCurrentPair(allPairs)
                }
            }
        }

        fun selectPair(pairId: Long) {
            if (_currentPairId.value == pairId) return
            _currentPairId.value = pairId
            syncCurrentPair(_pairs.value)
        }

        private fun syncCurrentPair(allPairs: List<PhotoPair>) {
            if (allPairs.isEmpty()) {
                _pair.value = null
                _pairNumber.value = 0
                return
            }

            val currentId = _currentPairId.value
            val index = allPairs.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: 0
            val selectedPair = allPairs[index]

            _currentPairId.value = selectedPair.id
            _pair.value = selectedPair
            _pairNumber.value = index + 1
        }

        fun prepareRetake() {
            viewModelScope.launch {
                _pair.value?.let { pair ->
                    photoPairRepository.update(
                        pair.copy(status = PairStatus.BEFORE_ONLY),
                    )
                    _retakeReady.emit(pair)
                }
            }
        }

        fun combinePair() {
            viewModelScope.launch {
                _isCombining.value = true
                try {
                    val targetPair = _pair.value ?: return@launch
                    combineImagesUseCase(targetPair.id)
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
