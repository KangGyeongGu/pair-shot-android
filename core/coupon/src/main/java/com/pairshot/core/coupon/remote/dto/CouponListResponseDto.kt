package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponListResponseDto(
    @SerialName("coupons") val coupons: List<CouponListItemDto> = emptyList(),
)
