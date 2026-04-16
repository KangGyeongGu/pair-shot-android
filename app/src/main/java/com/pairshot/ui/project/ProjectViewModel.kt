package com.pairshot.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.data.local.location.LocationProvider
import com.pairshot.data.local.location.LocationResult
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
        private val locationProvider: LocationProvider,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
        val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

        private val _currentLocation = MutableStateFlow<LocationResult?>(null)
        val currentLocation: StateFlow<LocationResult?> = _currentLocation.asStateFlow()

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

        fun fetchCurrentLocation() {
            viewModelScope.launch {
                _currentLocation.value = locationProvider.getCurrentLocation()
            }
        }

        fun createProject(name: String) {
            val location = _currentLocation.value
            val projectName =
                name.ifBlank {
                    location?.shortAddress ?: "새 프로젝트"
                }

            viewModelScope.launch {
                val now = System.currentTimeMillis()
                projectRepository.insert(
                    Project(
                        name = projectName,
                        address = location?.address,
                        latitude = location?.latitude,
                        longitude = location?.longitude,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
                _currentLocation.value = null
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
