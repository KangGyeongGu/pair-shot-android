package com.pairshot.data.local.db.entity

import com.pairshot.feature.project.domain.model.Project

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
