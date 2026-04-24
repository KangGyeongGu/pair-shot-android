package com.pairshot.core.coupon.remote

sealed interface StatusApiResult {
    data object Activated : StatusApiResult

    data object Revoked : StatusApiResult

    data object NotFoundOrForeign : StatusApiResult

    data object NetworkError : StatusApiResult

    data object ServerError : StatusApiResult
}
