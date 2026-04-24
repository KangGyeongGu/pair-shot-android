package com.pairshot.core.coupon.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CouponListItemDto(
    @SerialName("coupon_id") val couponId: String,
    @SerialName("short_code") val shortCode: String? = null,
    @SerialName("duration_days") val durationDays: Long? = null,
    @SerialName("status") val status: String,
    @SerialName("activated_at") val activatedAt: String,
    @SerialName("batch_label") val batchLabel: String? = null,
)
