package com.pairshot.core.model

data class IntegrityResult(
    val total: Int,
    val orphanedCount: Int,
    val orphanedPairIds: List<Long>,
)
