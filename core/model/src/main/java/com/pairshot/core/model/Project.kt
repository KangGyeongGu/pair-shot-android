package com.pairshot.core.model

data class Project(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val pairCount: Int = 0,
    val completedCount: Int = 0,
    val combinedCount: Int = 0,
)
