package com.pairshot.di.feature

import com.pairshot.core.domain.combine.CombineSettingsRepository
import com.pairshot.core.domain.settings.AppSettingsRepository
import com.pairshot.core.domain.settings.StorageRepository
import com.pairshot.core.domain.settings.WatermarkRepository
import com.pairshot.core.data.repository.CombineSettingsRepositoryImpl
import com.pairshot.core.data.repository.AppSettingsRepositoryImpl
import com.pairshot.core.data.repository.StorageRepositoryImpl
import com.pairshot.core.data.repository.WatermarkRepositoryImpl
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

    @Binds
    abstract fun bindCombineSettingsRepository(impl: CombineSettingsRepositoryImpl): CombineSettingsRepository
}
