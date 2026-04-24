package com.pairshot.core.coupon.remote

import com.pairshot.core.coupon.remote.dto.CouponListItemDto

sealed interface ListApiResult {
    data class Success(
        val coupons: List<CouponListItemDto>,
    ) : ListApiResult

    data object NetworkError : ListApiResult

    data object ServerError : ListApiResult
}
