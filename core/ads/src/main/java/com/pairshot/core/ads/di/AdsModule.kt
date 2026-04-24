package com.pairshot.core.ads.di

import com.pairshot.core.ads.BuildConfig
import com.pairshot.core.ads.config.AdsConfig
import com.pairshot.core.ads.config.ProductionAdsConfig
import com.pairshot.core.ads.config.TestAdsConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdsModule {
    @Provides
    @Singleton
    fun provideAdsConfig(): AdsConfig = if (BuildConfig.DEBUG) TestAdsConfig else ProductionAdsConfig
}
