package com.pairshot.data.repository.project

import com.pairshot.core.model.Project
import com.pairshot.core.domain.project.ProjectRepository
import com.pairshot.core.database.dao.ProjectDao
import com.pairshot.core.database.entity.toDomain
import com.pairshot.core.database.entity.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProjectRepositoryImpl
    @Inject
    constructor(
        private val projectDao: ProjectDao,
    ) : ProjectRepository {
        override fun getAll(): Flow<List<Project>> = projectDao.getAll().map { entities -> entities.map { it.toDomain() } }

        override suspend fun getById(id: Long): Project? =
            withContext(Dispatchers.IO) {
                projectDao.getById(id)?.toDomain()
            }

        override suspend fun insert(project: Project): Long =
            withContext(Dispatchers.IO) {
                projectDao.insert(project.toEntity())
            }

        override suspend fun update(project: Project) =
            withContext(Dispatchers.IO) {
                projectDao.update(project.toEntity())
            }

        override suspend fun delete(project: Project) =
            withContext(Dispatchers.IO) {
                projectDao.delete(project.toEntity())
            }
    }
