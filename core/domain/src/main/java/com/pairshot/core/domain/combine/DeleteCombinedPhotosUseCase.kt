package com.pairshot.core.domain.combine

import javax.inject.Inject

class DeleteCombinedPhotosUseCase
    @Inject
    constructor(
        private val combineHistoryRepository: CombineHistoryRepository,
    ) {
        suspend operator fun invoke(pairIds: List<Long>) {
            pairIds.forEach { pairId ->
                runCatching { combineHistoryRepository.deleteCombinedPhoto(pairId) }
            }
        }
    }
