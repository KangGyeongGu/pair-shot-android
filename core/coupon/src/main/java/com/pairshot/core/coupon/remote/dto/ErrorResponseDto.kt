package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseDto(
    @SerialName("error") val error: String? = null,
    @SerialName("message") val message: String? = null,
)
