package com.pairshot.app.di

import com.pairshot.core.ads.controller.InterstitialAdController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdsHostEntryPoint {
    fun interstitialAdController(): InterstitialAdController
}
