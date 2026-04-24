package com.pairshot.core.coupon.remote

import com.pairshot.core.coupon.remote.dto.ActivateRequestDto
import com.pairshot.core.coupon.remote.dto.StatusRequestDto

interface CouponActivationApi {
    suspend fun activate(request: ActivateRequestDto): ActivationApiResult

    suspend fun fetchStatus(request: StatusRequestDto): StatusApiResult

    suspend fun fetchMyCoupons(deviceHash: String): ListApiResult
}
