package com.pairshot.di

import com.pairshot.BuildConfig
import com.pairshot.core.domain.settings.AppInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppInfoModule {
    @Provides
    @Singleton
    fun provideAppInfo(): AppInfo =
        object : AppInfo {
            override val versionName: String = BuildConfig.VERSION_NAME
        }
}
