package com.pairshot.core.coupon.remote

import com.pairshot.core.coupon.remote.dto.ActivateResponseDto

sealed interface ActivationApiResult {
    data class Success(
        val response: ActivateResponseDto,
    ) : ActivationApiResult

    data object NotFound : ActivationApiResult

    data object AlreadyUsedOnAnotherDevice : ActivationApiResult

    data object Revoked : ActivationApiResult

    data object InvalidRequest : ActivationApiResult

    data object ServerError : ActivationApiResult

    data object NetworkError : ActivationApiResult
}
