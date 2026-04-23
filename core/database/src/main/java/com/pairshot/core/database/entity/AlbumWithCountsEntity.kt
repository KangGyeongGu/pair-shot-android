package com.pairshot.core.database.entity

import androidx.room.Embedded

data class AlbumWithCountsEntity(
    @Embedded val album: AlbumEntity,
    val pairCount: Int,
)
