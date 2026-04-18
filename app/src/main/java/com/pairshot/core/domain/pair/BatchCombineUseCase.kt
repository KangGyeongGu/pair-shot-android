package com.pairshot.core.domain.pair

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
                }
                onProgress(index + 1, pairIds.size)
            }
            return success
        }
    }
