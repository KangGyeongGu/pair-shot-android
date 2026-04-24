package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivateRequestDto(
    @SerialName("code") val code: String,
    @SerialName("device_hash") val deviceHash: String,
)
