package com.pairshot.core.domain.project

import com.pairshot.core.model.Project

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
            val total = pairs.size
            val failed =
                pairs.count { pair ->
                    runCatching { photoPairRepository.delete(pair) }.isFailure
                }
            if (failed > 0) {
                throw IllegalStateException("$failed/${total}건의 사진 삭제에 실패하여 프로젝트를 삭제할 수 없습니다")
            }
            projectRepository.delete(project)
        }
    }
