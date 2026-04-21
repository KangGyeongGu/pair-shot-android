package com.pairshot.feature.compare.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.pairshot.core.navigation.CombinedViewer
import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair
import com.pairshot.core.domain.pair.PhotoPairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CombinedViewerViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val photoPairRepository: PhotoPairRepository,
    ) : ViewModel() {
        private val initialPairId: Long = savedStateHandle.toRoute<CombinedViewer>().pairId

        private val _pair = MutableStateFlow<PhotoPair?>(null)
        val pair: StateFlow<PhotoPair?> = _pair.asStateFlow()

        private val _pairs = MutableStateFlow<List<PhotoPair>>(emptyList())
        val pairs: StateFlow<List<PhotoPair>> = _pairs.asStateFlow()

        private val _currentPairId = MutableStateFlow(initialPairId)
        val currentPairId: StateFlow<Long> = _currentPairId.asStateFlow()

        private val _pairNumber = MutableStateFlow(0)
        val pairNumber: StateFlow<Int> = _pairNumber.asStateFlow()

        init {
            observePairs()
        }

        private fun observePairs() {
            viewModelScope.launch {
                val loaded = photoPairRepository.getById(initialPairId) ?: return@launch

                photoPairRepository.getPairsByProject(loaded.projectId).collect { allPairs ->
                    val combinedPairs = allPairs.filter { it.status == PairStatus.COMBINED }
                    _pairs.value = combinedPairs
                    syncCurrentPair(combinedPairs)
                }
            }
        }

        fun selectPair(pairId: Long) {
            if (_currentPairId.value == pairId) return
            _currentPairId.value = pairId
            syncCurrentPair(_pairs.value)
        }

        private fun syncCurrentPair(combinedPairs: List<PhotoPair>) {
            if (combinedPairs.isEmpty()) {
                _pair.value = null
                _pairNumber.value = 0
                return
            }
            val currentId = _currentPairId.value
            val index = combinedPairs.indexOfFirst { it.id == currentId }.takeIf { it >= 0 } ?: 0
            val selected = combinedPairs[index]
            _currentPairId.value = selected.id
            _pair.value = selected
            _pairNumber.value = index + 1
        }
    }
