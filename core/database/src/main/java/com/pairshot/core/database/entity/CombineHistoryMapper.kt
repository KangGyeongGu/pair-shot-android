package com.pairshot.core.database.entity

import com.pairshot.core.model.CombineHistory

fun CombineHistoryEntity.toDomain() =
    CombineHistory(
        id = id,
        pairId = pairId,
        mediaStoreUri = mediaStoreUri,
    )

fun CombineHistory.toEntity() =
    CombineHistoryEntity(
        id = id,
        pairId = pairId,
        mediaStoreUri = mediaStoreUri,
    )
