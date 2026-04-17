package com.pairshot.di

import com.pairshot.data.repository.ExportRepositoryImpl
import com.pairshot.data.repository.PhotoPairRepositoryImpl
import com.pairshot.data.repository.ProjectRepositoryImpl
import com.pairshot.data.repository.StorageRepositoryImpl
import com.pairshot.data.repository.WatermarkRepositoryImpl
import com.pairshot.domain.repository.ExportRepository
import com.pairshot.domain.repository.PhotoPairRepository
import com.pairshot.domain.repository.ProjectRepository
import com.pairshot.domain.repository.StorageRepository
import com.pairshot.domain.repository.WatermarkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    abstract fun bindPhotoPairRepository(impl: PhotoPairRepositoryImpl): PhotoPairRepository

    @Binds
    abstract fun bindExportRepository(impl: ExportRepositoryImpl): ExportRepository

    @Binds
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository

    @Binds
    abstract fun bindWatermarkRepository(impl: WatermarkRepositoryImpl): WatermarkRepository
}
