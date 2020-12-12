package com.zionhuang.music.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.SongEntity

@Database(entities = [SongEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract val songDao: SongDao

    companion object {
        private const val DBNAME = "song.db"

        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            if (INSTANCE == null) {
                synchronized(MusicDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context, MusicDatabase::class.java, DBNAME).build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}