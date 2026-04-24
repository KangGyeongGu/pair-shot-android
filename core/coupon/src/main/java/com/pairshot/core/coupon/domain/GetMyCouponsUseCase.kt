package com.pairshot.core.coupon.domain

import javax.inject.Inject

class GetMyCouponsUseCase
    @Inject
    constructor(
        private val repository: CouponRepository,
    ) {
        suspend operator fun invoke(): List<CouponListItem> = repository.fetchMyCoupons()
    }
