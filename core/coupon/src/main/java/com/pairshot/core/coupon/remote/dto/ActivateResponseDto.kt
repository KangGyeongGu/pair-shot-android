package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateResponseDto(
    @SerialName("coupon_id") val couponId: String,
    @SerialName("duration_days") val durationDays: Long? = null,
    @SerialName("activated_at") val activatedAt: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)
