package com.pairshot.di.feature

import com.pairshot.data.repository.PhotoPairRepositoryImpl
import com.pairshot.domain.repository.PhotoPairRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PairRepositoryModule {
    @Binds
    abstract fun bindPhotoPairRepository(impl: PhotoPairRepositoryImpl): PhotoPairRepository
}
