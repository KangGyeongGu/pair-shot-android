package com.pairshot.core.coupon.domain

import kotlinx.coroutines.flow.Flow

interface CouponRepository {
    fun observeStatus(): Flow<CouponStatus>

    suspend fun activate(couponCode: String): ActivationResult

    suspend fun retryPendingIfAny()

    suspend fun syncStatus()

    suspend fun fetchMyCoupons(): List<CouponListItem>

    suspend fun clear()
}
