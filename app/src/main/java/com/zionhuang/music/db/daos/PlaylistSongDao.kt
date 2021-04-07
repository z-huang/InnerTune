package com.zionhuang.music.db.daos

import androidx.room.Dao
import androidx.room.Insert
import com.zionhuang.music.db.entities.PlaylistSongEntity

@Dao
interface PlaylistSongDao {
    @Insert
    fun insert(playlistSong: PlaylistSongEntity): Long
}