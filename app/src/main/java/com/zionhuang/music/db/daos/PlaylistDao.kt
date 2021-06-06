package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistSong
import com.zionhuang.music.db.entities.PlaylistSongEntity
import com.zionhuang.music.db.entities.PlaylistWithSongs

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylistsAsPagingSource(): PagingSource<Int, PlaylistEntity>

    @Query("SELECT * FROM playlist")
    fun getAllPlaylists(): List<PlaylistEntity>

    @Transaction
    @Query("SELECT * FROM playlist WHERE playlistId = :playlistId")
    fun getPlaylistWithSongs(playlistId: Int): PagingSource<Int, PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId ORDER BY idInPlaylist")
    fun getPlaylistSongs(playlistId: Int): PagingSource<Int, PlaylistSong>

    @Insert
    fun insert(playlist: PlaylistEntity)

    @Update
    fun updatePlaylist(playlist: PlaylistEntity)

    @Update
    fun updatePlaylistSongs(list: List<PlaylistSongEntity>)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)
}