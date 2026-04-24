package com.pairshot.core.coupon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.coupon.domain.ActivateCouponUseCase
import com.pairshot.core.coupon.domain.ActivationResult
import com.pairshot.core.coupon.domain.CouponStatus
import com.pairshot.core.coupon.domain.ObserveCouponStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponViewModel
    @Inject
    constructor(
        observeCouponStatusUseCase: ObserveCouponStatusUseCase,
        private val activateCouponUseCase: ActivateCouponUseCase,
    ) : ViewModel() {
        val status: StateFlow<CouponStatus> =
            observeCouponStatusUseCase().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS),
                initialValue = CouponStatus.None,
            )

        private val _activationState = MutableStateFlow<CouponActivationUiState>(CouponActivationUiState.Idle)
        val activationState: StateFlow<CouponActivationUiState> = _activationState.asStateFlow()

        fun activate(code: String) {
            if (code.isBlank()) {
                _activationState.value = CouponActivationUiState.Failure(ActivationResult.Failure.InvalidFormat)
                return
            }
            viewModelScope.launch {
                _activationState.value = CouponActivationUiState.Loading
                val result = activateCouponUseCase(code.trim())
                _activationState.value =
                    when (result) {
                        is ActivationResult.Success -> {
                            CouponActivationUiState.Success(result.coupon.duration.days)
                        }

                        is ActivationResult.Failure -> {
                            CouponActivationUiState.Failure(result)
                        }
                    }
            }
        }

        fun resetActivationState() {
            _activationState.value = CouponActivationUiState.Idle
        }

        private companion object {
            const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L
        }
    }
