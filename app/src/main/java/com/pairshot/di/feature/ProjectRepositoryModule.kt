package com.pairshot.di.feature

import com.pairshot.core.domain.project.ProjectRepository
import com.pairshot.core.data.repository.ProjectRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProjectRepositoryModule {
    @Binds
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository
}
