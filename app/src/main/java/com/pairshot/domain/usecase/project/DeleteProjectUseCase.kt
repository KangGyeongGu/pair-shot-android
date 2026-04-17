package com.pairshot.domain.usecase.project

import com.pairshot.domain.model.Project
import com.pairshot.domain.repository.PhotoPairRepository
import com.pairshot.domain.repository.ProjectRepository
import javax.inject.Inject

class DeleteProjectUseCase
    @Inject
    constructor(
        private val projectRepository: ProjectRepository,
        private val photoPairRepository: PhotoPairRepository,
    ) {
        suspend operator fun invoke(project: Project) {
            // MediaStore 파일 포함 삭제를 위해 각 페어를 개별 삭제
            // stale URI 등으로 개별 삭제 실패해도 프로젝트 삭제는 보장
            val pairs = photoPairRepository.getAllByProjectOnce(project.id)
            pairs.forEach { pair ->
                try {
                    photoPairRepository.delete(pair)
                } catch (_: Exception) {
                    // MediaStore URI가 이미 삭제된 경우 등 — 무시하고 계속 진행
                }
            }
            // Room CASCADE가 남은 PhotoPairEntity를 정리
            projectRepository.delete(project)
        }
    }
