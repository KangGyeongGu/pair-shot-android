package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateResponseDto(
    @SerialName("duration_days") val durationDays: Long? = null,
    @SerialName("activated_at") val activatedAt: String,
    @SerialName("expires_at") val expiresAt: String? = null,
)
