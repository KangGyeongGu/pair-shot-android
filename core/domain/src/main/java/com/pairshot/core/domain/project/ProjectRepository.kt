package com.pairshot.core.domain.project

import com.pairshot.core.model.Project
import com.pairshot.core.model.ProjectSortOrder
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAll(sortOrder: ProjectSortOrder = ProjectSortOrder.UPDATED_DESC): Flow<List<Project>>

    suspend fun getById(id: Long): Project?

    suspend fun insert(project: Project): Long

    suspend fun update(project: Project)

    suspend fun delete(project: Project)
}
