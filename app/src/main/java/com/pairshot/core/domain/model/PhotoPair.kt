package com.pairshot.core.domain.model

data class PhotoPair(
    val id: Long = 0,
    val projectId: Long,
    val label: String = "",
    val beforePhotoUri: String,
    val afterPhotoUri: String? = null,
    val combinedPhotoUri: String? = null,
    val beforeTimestamp: Long,
    val afterTimestamp: Long? = null,
    val status: PairStatus,
    val zoomLevel: Float? = null,
    val lensId: String? = null,
)
