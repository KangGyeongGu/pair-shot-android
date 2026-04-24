package com.pairshot.core.coupon.crypto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponPayload(
    @SerialName("coupon_id") val couponId: String,
    @SerialName("duration_days") val durationDays: Long?,
    @SerialName("iat") val issuedAtEpochSeconds: Long,
)
