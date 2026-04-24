package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponseDto(
    @SerialName("status") val status: String,
    @SerialName("duration_days") val durationDays: Long? = null,
)
