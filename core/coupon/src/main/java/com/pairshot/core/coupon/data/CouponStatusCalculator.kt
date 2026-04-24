package com.pairshot.core.coupon.data

import com.pairshot.core.coupon.domain.Coupon
import com.pairshot.core.coupon.domain.CouponDuration
import com.pairshot.core.coupon.domain.CouponStatus
import com.pairshot.core.coupon.local.StoredCouponState

internal object CouponStatusCalculator {
    private const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L

    fun toStatus(
        stored: StoredCouponState?,
        nowMillis: Long,
    ): CouponStatus {
        if (stored == null) return CouponStatus.None
        val duration = CouponDuration.fromDays(stored.durationDays)
        val coupon =
            Coupon(
                id = stored.couponId,
                duration = duration,
                activatedAtEpochMillis = stored.activatedAtEpochMillis,
            )
        val days = duration.days
        if (days == null) {
            return CouponStatus.Active(coupon = coupon, expiresAtEpochMillis = null)
        }
        val expiresAt = stored.activatedAtEpochMillis + days * MILLIS_PER_DAY
        return if (nowMillis < expiresAt) {
            CouponStatus.Active(coupon = coupon, expiresAtEpochMillis = expiresAt)
        } else {
            CouponStatus.Expired(coupon = coupon)
        }
    }

    fun isAdFree(
        stored: StoredCouponState?,
        nowMillis: Long,
    ): Boolean = toStatus(stored, nowMillis) is CouponStatus.Active
}
