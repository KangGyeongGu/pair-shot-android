package com.pairshot.di.feature

import com.pairshot.feature.settings.data.repository.AppSettingsRepositoryImpl
import com.pairshot.feature.settings.data.repository.StorageRepositoryImpl
import com.pairshot.feature.settings.data.repository.WatermarkRepositoryImpl
import com.pairshot.feature.settings.domain.repository.AppSettingsRepository
import com.pairshot.feature.settings.domain.repository.StorageRepository
import com.pairshot.feature.settings.domain.repository.WatermarkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsRepositoryModule {
    @Binds
    abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository

    @Binds
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository

    @Binds
    abstract fun bindWatermarkRepository(impl: WatermarkRepositoryImpl): WatermarkRepository
}
