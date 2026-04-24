package com.pairshot.core.coupon.domain

data class Coupon(
    val id: String,
    val duration: CouponDuration,
    val activatedAtEpochMillis: Long,
)
