package com.pairshot.core.coupon.domain

sealed interface ActivationResult {
    data class Success(
        val coupon: Coupon,
        val expiresAtEpochMillis: Long?,
    ) : ActivationResult

    sealed interface Failure : ActivationResult {
        data object InvalidFormat : Failure

        data object InvalidSignature : Failure

        data object AlreadyUsedOnAnotherDevice : Failure

        data object Revoked : Failure

        data object NotFound : Failure

        data object NetworkError : Failure

        data object UnknownError : Failure
    }
}
