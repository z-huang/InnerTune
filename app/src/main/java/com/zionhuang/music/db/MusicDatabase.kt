package com.zionhuang.music.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zionhuang.music.db.daos.*
import com.zionhuang.music.db.entities.*

@Database(entities = [
    SongEntity::class,
    ArtistEntity::class,
    PlaylistEntity::class,
    PlaylistSongEntity::class,
    DownloadEntity::class
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract val songDao: SongDao
    abstract val artistDao: ArtistDao
    abstract val playlistDao: PlaylistDao
    abstract val playlistSongDao: PlaylistSongDao
    abstract val downloadDao: DownloadDao

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