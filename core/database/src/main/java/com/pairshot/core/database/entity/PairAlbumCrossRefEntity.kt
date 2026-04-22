package com.pairshot.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "pair_album_cross_ref",
    primaryKeys = ["pairId", "albumId"],
    foreignKeys = [
        ForeignKey(
            entity = PhotoPairEntity::class,
            parentColumns = ["id"],
            childColumns = ["pairId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("pairId"), Index("albumId")],
)
data class PairAlbumCrossRefEntity(
    val pairId: Long,
    val albumId: Long,
)
