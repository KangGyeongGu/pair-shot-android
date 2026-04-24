package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateRequestDto(
    @SerialName("coupon_id") val couponId: String,
    @SerialName("signature_b58") val signatureB58: String,
    @SerialName("device_hash") val deviceHash: String,
)
