package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponListRequestDto(
    @SerialName("device_hash") val deviceHash: String,
)
