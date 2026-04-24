package com.pairshot.core.coupon.crypto

sealed interface CouponParseError {
    data object InvalidFormat : CouponParseError

    data object TypeMismatch : CouponParseError
}
