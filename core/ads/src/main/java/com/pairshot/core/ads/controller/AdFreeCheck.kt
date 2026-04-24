package com.pairshot.core.ads.controller

import com.pairshot.core.domain.coupon.AdFreeStatusProvider
import kotlinx.coroutines.flow.first

internal suspend fun AdFreeStatusProvider.currentIsAdFree(): Boolean = runCatching { observeIsAdFree().first() }.getOrDefault(false)
