package com.pairshot.core.domain.project

import com.pairshot.core.domain.pair.PhotoPairRepository
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
