package com.pairshot.core.coupon.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCouponStatusUseCase
    @Inject
    constructor(
        private val repository: CouponRepository,
    ) {
        operator fun invoke(): Flow<CouponStatus> = repository.observeStatus()
    }
