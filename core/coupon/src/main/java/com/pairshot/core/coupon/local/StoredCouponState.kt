package com.pairshot.core.coupon.local

data class StoredCouponState(
    val latestCouponId: String,
    val firstActivatedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long?,
)
