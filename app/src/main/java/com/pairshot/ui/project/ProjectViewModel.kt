package com.pairshot.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.domain.model.Project
import com.pairshot.domain.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProjectUiState {
    data object Loading : ProjectUiState

    data class Success(
        val projects: List<Project>,
    ) : ProjectUiState

    data class Error(
        val message: String,
    ) : ProjectUiState
}

@HiltViewModel
class ProjectViewModel
    @Inject
    constructor(
        private val projectRepository: ProjectRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
        val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

        init {
            loadProjects()
        }

        private fun loadProjects() {
            viewModelScope.launch {
                projectRepository
                    .getAll()
                    .catch { e -> _uiState.value = ProjectUiState.Error(e.message ?: "알 수 없는 오류") }
                    .collect { projects ->
                        _uiState.value = ProjectUiState.Success(projects)
                    }
            }
        }

        fun createProject(
            name: String,
            address: String? = null,
            latitude: Double? = null,
            longitude: Double? = null,
        ) {
            viewModelScope.launch {
                val now = System.currentTimeMillis()
                val projectName = name.ifBlank { address ?: "새 프로젝트" }
                projectRepository.insert(
                    Project(
                        name = projectName,
                        address = address,
                        latitude = latitude,
                        longitude = longitude,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            }
        }

        fun renameProject(
            project: Project,
            newName: String,
        ) {
            viewModelScope.launch {
                projectRepository.update(
                    project.copy(
                        name = newName,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
        }

        fun deleteProject(project: Project) {
            viewModelScope.launch {
                projectRepository.delete(project)
            }
        }
    }
