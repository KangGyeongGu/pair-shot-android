package com.pairshot.data.local.db.entity

import com.pairshot.core.domain.project.Project

data class ProjectWithCountsEntity(
    val id: Long,
    val name: String,
    val description: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: Long,
    val updatedAt: Long,
    val pairCount: Int,
    val completedCount: Int,
)

fun ProjectWithCountsEntity.toDomain() =
    Project(
        id = id,
        name = name,
        description = description,
        address = address,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
        pairCount = pairCount,
        completedCount = completedCount,
    )
