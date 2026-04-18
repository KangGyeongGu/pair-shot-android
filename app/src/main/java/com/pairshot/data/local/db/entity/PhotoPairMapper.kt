package com.pairshot.data.local.db.entity

import com.pairshot.core.domain.model.PairStatus
import com.pairshot.core.domain.model.PhotoPair

fun PhotoPairEntity.toDomain() =
    PhotoPair(
        id = id,
        projectId = projectId,
        label = label,
        beforePhotoUri = beforePhotoUri,
        afterPhotoUri = afterPhotoUri,
        combinedPhotoUri = combinedPhotoUri,
        beforeTimestamp = beforeTimestamp,
        afterTimestamp = afterTimestamp,
        status = PairStatus.valueOf(status),
        zoomLevel = zoomLevel,
        lensId = lensId,
    )

fun PhotoPair.toEntity() =
    PhotoPairEntity(
        id = id,
        projectId = projectId,
        label = label,
        beforePhotoUri = beforePhotoUri,
        afterPhotoUri = afterPhotoUri,
        combinedPhotoUri = combinedPhotoUri,
        beforeTimestamp = beforeTimestamp,
        afterTimestamp = afterTimestamp,
        status = status.name,
        zoomLevel = zoomLevel,
        lensId = lensId,
    )
