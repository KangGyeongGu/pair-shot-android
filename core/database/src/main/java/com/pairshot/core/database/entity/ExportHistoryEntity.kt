package com.pairshot.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "export_history",
    foreignKeys = [
        ForeignKey(
            entity = PhotoPairEntity::class,
            parentColumns = ["id"],
            childColumns = ["pairId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["pairId"]),
        Index(value = ["pairId", "kind"]),
    ],
)
data class ExportHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pairId: Long,
    val mediaStoreUri: String,
    val kind: String,
    val createdAt: Long,
)
