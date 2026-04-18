package com.pairshot.feature.pair.domain.usecase

import javax.inject.Inject

class BatchCombineUseCase
    @Inject
    constructor(
        private val combineImagesUseCase: CombineImagesUseCase,
    ) {
        suspend operator fun invoke(
            pairIds: List<Long>,
            onProgress: (current: Int, total: Int) -> Unit,
        ): Int {
            var success = 0
            pairIds.forEachIndexed { index, id ->
                try {
                    combineImagesUseCase(id)
                    success++
                } catch (_: Exception) {
                    // 개별 실패 건너뜀
                }
                onProgress(index + 1, pairIds.size)
            }
            return success
        }
    }
