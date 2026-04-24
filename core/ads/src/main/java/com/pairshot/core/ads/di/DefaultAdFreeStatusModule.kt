package com.pairshot.core.ads.di

import com.pairshot.core.domain.coupon.AdFreeStatusProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DefaultAdFreeStatusModule {
    @Provides
    @Singleton
    fun provideDefaultAdFreeStatusProvider(): AdFreeStatusProvider =
        object : AdFreeStatusProvider {
            override fun observeIsAdFree(): Flow<Boolean> = flowOf(false)
        }
}
