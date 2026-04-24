package com.pairshot.core.coupon.config

interface CouponApiConfig {
    val baseUrl: String
    val activatePath: String
    val statusPath: String
    val authHeaderName: String?
    val authHeaderValue: String?
    val timeoutMillis: Long
}
