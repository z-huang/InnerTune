package com.zionhuang.music.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zionhuang.music.db.daos.ArtistDao
import com.zionhuang.music.db.daos.DownloadDao
import com.zionhuang.music.db.daos.PlaylistDao
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.getApplication

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
    abstract val downloadDao: DownloadDao

    companion object {
        private const val DBNAME = "song.db"

        @Volatile
        var INSTANCE: MusicDatabase? = null

        fun getInstance(): MusicDatabase {
            if (INSTANCE == null) {
                synchronized(MusicDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(getApplication(), MusicDatabase::class.java, DBNAME).build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}