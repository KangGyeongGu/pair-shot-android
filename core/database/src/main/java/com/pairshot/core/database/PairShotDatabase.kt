package com.pairshot.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pairshot.core.database.dao.PhotoPairDao
import com.pairshot.core.database.dao.ProjectDao
import com.pairshot.core.database.entity.PhotoPairEntity
import com.pairshot.core.database.entity.ProjectEntity

@Database(
    entities = [ProjectEntity::class, PhotoPairEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PairShotDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    abstract fun photoPairDao(): PhotoPairDao
}
