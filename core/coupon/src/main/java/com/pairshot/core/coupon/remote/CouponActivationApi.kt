package com.pairshot.core.coupon.remote

import com.pairshot.core.coupon.remote.dto.ActivateRequestDto

interface CouponActivationApi {
    suspend fun activate(request: ActivateRequestDto): ActivationApiResult
}
