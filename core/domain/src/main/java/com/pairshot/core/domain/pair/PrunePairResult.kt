package com.pairshot.core.domain.pair

sealed interface PrunePairResult {
    data object Healthy : PrunePairResult

    data object BeforeDropped : PrunePairResult

    data object AfterDropped : PrunePairResult

    data object DeletedEntirely : PrunePairResult

    data object NotFound : PrunePairResult
}
