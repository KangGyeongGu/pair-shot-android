package com.pairshot.core.domain.pair

import com.pairshot.core.domain.combine.CombineHistoryRepository
import com.pairshot.core.model.PhotoPair
import javax.inject.Inject

data class DeletePairsResult(
    val deleted: Int,
    val failed: Int,
)

class DeletePairsUseCase
    @Inject
    constructor(
        private val photoPairRepository: PhotoPairRepository,
        private val combineHistoryRepository: CombineHistoryRepository,
    ) {
        suspend operator fun invoke(pairs: List<PhotoPair>): DeletePairsResult {
            var deleted = 0
            var failed = 0
            pairs.forEach { pair ->
                val outcome =
                    runCatching {
                        runCatching { combineHistoryRepository.deleteCombinedPhoto(pair.id) }
                        photoPairRepository.delete(pair)
                    }
                if (outcome.isSuccess) deleted++ else failed++
            }
            return DeletePairsResult(deleted = deleted, failed = failed)
        }
    }
