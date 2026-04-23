package com.pairshot.core.database.entity

import com.pairshot.core.model.Album

fun AlbumEntity.toDomain(pairCount: Int = 0) =
    Album(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pairCount = pairCount,
    )

fun AlbumWithCountsEntity.toDomain() = album.toDomain(pairCount = pairCount)

fun Album.toEntity() =
    AlbumEntity(
        id = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
