package com.pairshot.core.coupon.domain

import javax.inject.Inject

class ActivateCouponUseCase
    @Inject
    constructor(
        private val repository: CouponRepository,
    ) {
        suspend operator fun invoke(couponCode: String): ActivationResult = repository.activate(couponCode)
    }
