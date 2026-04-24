package com.pairshot.core.coupon.domain

data class CouponListItem(
    val couponId: String,
    val shortCode: String?,
    val durationDays: Long?,
    val status: CouponListItemStatus,
    val activatedAtEpochMillis: Long,
    val batchLabel: String?,
)

enum class CouponListItemStatus { ACTIVATED, EXPIRED, REVOKED }
