package com.pairshot.core.domain.pair

sealed interface PairNavigationTarget {
    data class AfterCamera(
        val pairId: Long,
    ) : PairNavigationTarget

    data class BeforeRetakeCamera(
        val pairId: Long,
    ) : PairNavigationTarget

    data class PairPreview(
        val pairId: Long,
    ) : PairNavigationTarget
}
