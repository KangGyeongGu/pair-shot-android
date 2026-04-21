package com.pairshot.core.database.entity

import com.pairshot.core.model.Project

fun ProjectEntity.toDomain() =
    Project(
        id = id,
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun Project.toEntity() =
    ProjectEntity(
        id = id,
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
