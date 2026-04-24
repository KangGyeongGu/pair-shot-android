package com.pairshot.core.coupon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.coupon.domain.ActivateCouponUseCase
import com.pairshot.core.coupon.domain.ActivationResult
import com.pairshot.core.coupon.domain.CouponListItem
import com.pairshot.core.coupon.domain.CouponStatus
import com.pairshot.core.coupon.domain.GetMyCouponsUseCase
import com.pairshot.core.coupon.domain.ObserveCouponStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CouponViewModel
    @Inject
    constructor(
        observeCouponStatusUseCase: ObserveCouponStatusUseCase,
        private val activateCouponUseCase: ActivateCouponUseCase,
        private val getMyCouponsUseCase: GetMyCouponsUseCase,
    ) : ViewModel() {
        val status: StateFlow<CouponStatus> =
            observeCouponStatusUseCase().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(WHILE_SUBSCRIBED_TIMEOUT_MS),
                initialValue = CouponStatus.None,
            )

        private val _activationState = MutableStateFlow<CouponActivationUiState>(CouponActivationUiState.Idle)
        val activationState: StateFlow<CouponActivationUiState> = _activationState.asStateFlow()

        private val _myCoupons = MutableStateFlow<List<CouponListItem>>(emptyList())
        val myCoupons: StateFlow<List<CouponListItem>> = _myCoupons.asStateFlow()

        private val _myCouponsLoading = MutableStateFlow(false)
        val myCouponsLoading: StateFlow<Boolean> = _myCouponsLoading.asStateFlow()

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

        fun loadMyCoupons() {
            viewModelScope.launch {
                _myCouponsLoading.value = true
                runCatching { getMyCouponsUseCase() }
                    .onSuccess { _myCoupons.value = it }
                    .onFailure { Timber.w(it, "fetch my coupons failed") }
                _myCouponsLoading.value = false
            }
        }

        private companion object {
            const val WHILE_SUBSCRIBED_TIMEOUT_MS = 5_000L
        }
    }
