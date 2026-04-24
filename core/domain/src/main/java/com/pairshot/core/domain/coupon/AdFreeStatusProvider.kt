package com.pairshot.core.domain.coupon

import kotlinx.coroutines.flow.Flow

interface AdFreeStatusProvider {
    fun observeIsAdFree(): Flow<Boolean>
}
