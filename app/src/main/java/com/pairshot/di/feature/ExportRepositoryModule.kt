package com.pairshot.di.feature

import com.pairshot.core.domain.repository.ExportRepository
import com.pairshot.data.repository.ExportRepositoryImpl
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
