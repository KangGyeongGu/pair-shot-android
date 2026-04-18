package com.pairshot.core.domain.settings

data class IntegrityResult(
    val total: Int,
    val orphanedCount: Int,
    val orphanedPairIds: List<Long>,
)
