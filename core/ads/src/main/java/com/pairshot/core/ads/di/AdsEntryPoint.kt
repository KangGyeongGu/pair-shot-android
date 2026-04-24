package com.pairshot.core.ads.di

import com.pairshot.core.ads.config.AdsConfig
import com.pairshot.core.ads.controller.InterstitialAdController
import com.pairshot.core.domain.coupon.AdFreeStatusProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdsEntryPoint {
    fun adsConfig(): AdsConfig

    fun adFreeStatusProvider(): AdFreeStatusProvider

    fun interstitialAdController(): InterstitialAdController
}
