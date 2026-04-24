package com.pairshot.core.coupon.data

import com.pairshot.core.coupon.local.CouponPreferencesSource
import com.pairshot.core.domain.coupon.AdFreeStatusProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponAdFreeStatusProvider
    @Inject
    constructor(
        private val preferences: CouponPreferencesSource,
    ) : AdFreeStatusProvider {
        override fun observeIsAdFree(): Flow<Boolean> =
            preferences.state
                .map { stored -> CouponStatusCalculator.isAdFree(stored, System.currentTimeMillis()) }
                .distinctUntilChanged()
    }
