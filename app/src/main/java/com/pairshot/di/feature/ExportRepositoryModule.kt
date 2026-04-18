package com.pairshot.di.feature

import com.pairshot.data.repository.ExportRepositoryImpl
import com.pairshot.domain.repository.ExportRepository
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
