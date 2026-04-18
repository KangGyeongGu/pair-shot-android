package com.pairshot.feature.project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pairshot.core.infra.location.LocationProvider
import com.pairshot.core.infra.location.LocationResult
import com.pairshot.feature.project.domain.model.Project
import com.pairshot.feature.project.domain.repository.ProjectRepository
import com.pairshot.feature.project.domain.usecase.DeleteProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProjectGroupMode {
    NONE,
    CREATED_DATE,
    UPDATED_DATE,
}

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
        private val deleteProjectUseCase: DeleteProjectUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<ProjectUiState>(ProjectUiState.Loading)
        val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

        private val _currentLocation = MutableStateFlow<LocationResult?>(null)
        val currentLocation: StateFlow<LocationResult?> = _currentLocation.asStateFlow()

        private val _selectionMode = MutableStateFlow(false)
        val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()

        private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
        val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

        private val _groupMode = MutableStateFlow(ProjectGroupMode.CREATED_DATE)
        val groupMode: StateFlow<ProjectGroupMode> = _groupMode.asStateFlow()

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
                        val validIds = projects.map { it.id }.toSet()
                        _selectedIds.value = _selectedIds.value.intersect(validIds)
                        if (_selectionMode.value && _selectedIds.value.isEmpty()) {
                            _selectionMode.value = false
                        }
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
                deleteProjectUseCase(project)
            }
        }

        fun enterSelectionMode() {
            _selectionMode.value = true
        }

        fun exitSelectionMode() {
            _selectionMode.value = false
            _selectedIds.value = emptySet()
        }

        fun toggleSelection(projectId: Long) {
            if (!_selectionMode.value) {
                _selectionMode.value = true
            }
            val next =
                _selectedIds.value.toMutableSet().apply {
                    if (!add(projectId)) remove(projectId)
                }
            _selectedIds.value = next
            if (next.isEmpty()) {
                _selectionMode.value = false
            }
        }

        fun selectAll() {
            val projects = (_uiState.value as? ProjectUiState.Success)?.projects ?: return
            _selectionMode.value = true
            _selectedIds.value = projects.map { it.id }.toSet()
        }

        fun setGroupMode(mode: ProjectGroupMode) {
            _groupMode.value = mode
        }

        fun deleteSelected() {
            val state = _uiState.value as? ProjectUiState.Success ?: return
            val targets = state.projects.filter { it.id in _selectedIds.value }
            if (targets.isEmpty()) {
                exitSelectionMode()
                return
            }

            viewModelScope.launch {
                targets.forEach { project ->
                    deleteProjectUseCase(project)
                }
                exitSelectionMode()
            }
        }
    }
