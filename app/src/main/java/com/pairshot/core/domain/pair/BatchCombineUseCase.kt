package com.pairshot.core.domain.pair

import javax.inject.Inject

data class BatchCombineResult(
    val successCount: Int,
    val failedIds: List<Long>,
)

class BatchCombineUseCase
    @Inject
    constructor(
        private val combineImagesUseCase: CombineImagesUseCase,
    ) {
        suspend operator fun invoke(
            pairIds: List<Long>,
            onProgress: (current: Int, total: Int) -> Unit,
        ): BatchCombineResult {
            var successCount = 0
            val failedIds = mutableListOf<Long>()
            pairIds.forEachIndexed { index, id ->
                try {
                    combineImagesUseCase(id)
                    successCount++
                } catch (_: Exception) {
                    failedIds.add(id)
                }
                onProgress(index + 1, pairIds.size)
            }
            return BatchCombineResult(successCount, failedIds)
        }
    }
