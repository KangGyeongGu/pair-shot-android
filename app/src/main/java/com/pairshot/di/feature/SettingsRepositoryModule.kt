package com.pairshot.di.feature

import com.pairshot.data.repository.AppSettingsRepositoryImpl
import com.pairshot.data.repository.StorageRepositoryImpl
import com.pairshot.data.repository.WatermarkRepositoryImpl
import com.pairshot.domain.repository.AppSettingsRepository
import com.pairshot.domain.repository.StorageRepository
import com.pairshot.domain.repository.WatermarkRepository
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
