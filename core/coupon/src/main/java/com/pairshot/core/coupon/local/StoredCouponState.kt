package com.pairshot.core.coupon.local

data class StoredCouponState(
    val couponId: String,
    val durationDays: Long?,
    val activatedAtEpochMillis: Long,
)
