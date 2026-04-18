package com.pairshot.core.domain.model

data class IntegrityResult(
    val total: Int,
    val orphanedCount: Int,
    val orphanedPairIds: List<Long>,
)
