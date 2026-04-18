package com.pairshot.core.domain.usecase

import com.pairshot.core.domain.model.Project
import com.pairshot.core.domain.repository.PhotoPairRepository
import com.pairshot.core.domain.repository.ProjectRepository
import javax.inject.Inject

class DeleteProjectUseCase
    @Inject
    constructor(
        private val projectRepository: ProjectRepository,
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(project: Project) {
            val pairs = photoPairRepository.getAllByProjectOnce(project.id)
            pairs.forEach { pair ->
                try {
                    photoPairRepository.delete(pair)
                } catch (_: Exception) {
                }
            }
            projectRepository.delete(project)
        }
    }
