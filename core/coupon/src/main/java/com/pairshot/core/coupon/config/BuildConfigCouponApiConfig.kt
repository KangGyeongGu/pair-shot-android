package com.pairshot.core.coupon.config

import com.pairshot.core.coupon.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildConfigCouponApiConfig
    @Inject
    constructor() : CouponApiConfig {
        override val baseUrl: String = BuildConfig.COUPON_API_BASE_URL
        override val activatePath: String = "/coupons/activate"
        override val statusPath: String = "/coupons/status"
        override val authHeaderName: String? = if (BuildConfig.COUPON_API_AUTH_KEY.isNotBlank()) "Authorization" else null
        override val authHeaderValue: String? =
            if (BuildConfig.COUPON_API_AUTH_KEY.isNotBlank()) "Bearer ${BuildConfig.COUPON_API_AUTH_KEY}" else null
        override val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS

        private companion object {
            const val DEFAULT_TIMEOUT_MILLIS = 10_000L
        }
    }
