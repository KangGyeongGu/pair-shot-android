package com.pairshot.core.database.entity

import androidx.room.Embedded

data class PhotoPairWithCountsEntity(
    @Embedded val pair: PhotoPairEntity,
    val hasCombined: Boolean,
)
