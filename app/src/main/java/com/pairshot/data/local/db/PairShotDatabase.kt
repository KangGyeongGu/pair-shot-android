package com.pairshot.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pairshot.data.local.db.dao.PhotoPairDao
import com.pairshot.data.local.db.dao.ProjectDao
import com.pairshot.data.local.db.entity.PhotoPairEntity
import com.pairshot.data.local.db.entity.ProjectEntity

@Database(
    entities = [ProjectEntity::class, PhotoPairEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PairShotDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    abstract fun photoPairDao(): PhotoPairDao
}
