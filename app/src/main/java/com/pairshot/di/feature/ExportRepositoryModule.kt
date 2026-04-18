package com.pairshot.di.feature

import com.pairshot.feature.export.data.repository.ExportRepositoryImpl
import com.pairshot.feature.export.domain.repository.ExportRepository
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
