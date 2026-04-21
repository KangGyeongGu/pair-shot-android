package com.pairshot.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.model.CombineConfig
import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.model.WatermarkConfig
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.rendering.WatermarkRenderer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CombineSettingsViewModel
    @Inject
    constructor(
        private val combineSettingsRepository: CombineSettingsRepository,
        private val watermarkRepository: WatermarkRepository,
        val watermarkRenderer: WatermarkRenderer,
    ) : ViewModel() {
        val combineConfig: StateFlow<CombineConfig> =
            combineSettingsRepository.configFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = CombineConfig(),
            )

        val watermarkConfig: StateFlow<WatermarkConfig> =
            watermarkRepository.watermarkConfigFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WatermarkConfig(),
            )

        fun updateCombineConfig(config: CombineConfig) {
            viewModelScope.launch {
                combineSettingsRepository.saveConfig(config)
            }
        }
    }
