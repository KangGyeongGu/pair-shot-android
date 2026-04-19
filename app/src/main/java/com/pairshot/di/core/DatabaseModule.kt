package com.pairshot.di.core

import android.content.Context
import androidx.room.Room
import com.pairshot.data.local.db.PairShotDatabase
import com.pairshot.data.local.db.dao.PhotoPairDao
import com.pairshot.data.local.db.dao.ProjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): PairShotDatabase =
        Room
            .databaseBuilder(context, PairShotDatabase::class.java, "pairshot.db")
            .build()

    @Provides
    @Singleton
    fun provideProjectDao(db: PairShotDatabase): ProjectDao = db.projectDao()

    @Provides
    @Singleton
    fun providePhotoPairDao(db: PairShotDatabase): PhotoPairDao = db.photoPairDao()
}
