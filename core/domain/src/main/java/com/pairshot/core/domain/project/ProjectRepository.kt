package com.pairshot.core.domain.project

import com.pairshot.core.model.Project

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getAll(): Flow<List<Project>>

    suspend fun getById(id: Long): Project?

    suspend fun insert(project: Project): Long

    suspend fun update(project: Project)

    suspend fun delete(project: Project)
}
