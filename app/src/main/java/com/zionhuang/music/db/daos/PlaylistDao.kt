package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zionhuang.music.db.entities.PlaylistEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylistsAsPagingSource(): PagingSource<Int, PlaylistEntity>

    @Query("SELECT * FROM playlist")
    fun getAllPlaylists(): List<PlaylistEntity>

    @Insert
    fun insert(playlist: PlaylistEntity): Long
}