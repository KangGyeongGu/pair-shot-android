package com.pairshot.core.coupon.local

data class PendingCouponActivation(
    val code: String,
    val sinceEpochMillis: Long,
)
