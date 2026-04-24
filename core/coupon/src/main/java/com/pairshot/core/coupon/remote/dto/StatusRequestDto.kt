package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusRequestDto(
    @SerialName("coupon_id") val couponId: String,
    @SerialName("device_hash") val deviceHash: String,
)
