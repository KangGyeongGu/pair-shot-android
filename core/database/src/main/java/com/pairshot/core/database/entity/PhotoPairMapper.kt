package com.pairshot.core.database.entity

import com.pairshot.core.model.PairStatus
import com.pairshot.core.model.PhotoPair

fun PhotoPairEntity.toDomain(hasCombined: Boolean = false) =
    PhotoPair(
        id = id,
        beforePhotoUri = beforePhotoUri,
        afterPhotoUri = afterPhotoUri,
        beforeTimestamp = beforeTimestamp,
        afterTimestamp = afterTimestamp,
        status = PairStatus.entries.firstOrNull { it.name == status } ?: PairStatus.BEFORE_ONLY,
        zoomLevel = zoomLevel,
        hasCombined = hasCombined,
    )

fun PhotoPairWithCountsEntity.toDomain() = pair.toDomain(hasCombined = hasCombined)

fun PhotoPair.toEntity() =
    PhotoPairEntity(
        id = id,
        beforePhotoUri = beforePhotoUri,
        afterPhotoUri = afterPhotoUri,
        beforeTimestamp = beforeTimestamp,
        afterTimestamp = afterTimestamp,
        status = status.name,
        zoomLevel = zoomLevel,
    )
