package com.zionhuang.music.di

import android.content.Context
import androidx.room.Room
import com.zionhuang.music.db.InternalDatabase
import com.zionhuang.music.db.MIGRATION_1_2
import com.zionhuang.music.db.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase {
        return MusicDatabase(
            delegate = Room.databaseBuilder(context, InternalDatabase::class.java, InternalDatabase.DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
        )
    }
}
