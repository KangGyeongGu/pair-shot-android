package com.pairshot.core.data.di

import com.pairshot.core.domain.export.ExportRepository
import com.pairshot.core.data.repository.ExportRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExportRepositoryModule {
    @Binds
    abstract fun bindExportRepository(impl: ExportRepositoryImpl): ExportRepository
}
