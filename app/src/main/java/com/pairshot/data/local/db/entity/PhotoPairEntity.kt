package com.pairshot.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "photo_pairs",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("projectId")],
)
data class PhotoPairEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val label: String = "",
    val beforePhotoUri: String,
    val afterPhotoUri: String? = null,
    val combinedPhotoUri: String? = null,
    val beforeTimestamp: Long = System.currentTimeMillis(),
    val afterTimestamp: Long? = null,
    val status: String = "BEFORE_ONLY",
    val zoomLevel: Float? = null,
    val lensId: String? = null,
)
