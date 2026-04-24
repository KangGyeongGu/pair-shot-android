package com.pairshot.core.coupon.ui

import com.pairshot.core.coupon.domain.ActivationResult

sealed interface CouponActivationUiState {
    data object Idle : CouponActivationUiState

    data object Loading : CouponActivationUiState

    data class Success(
        val durationDays: Long?,
    ) : CouponActivationUiState

    data class Failure(
        val failure: ActivationResult.Failure,
    ) : CouponActivationUiState
}
