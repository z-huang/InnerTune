package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.PlaylistEntity
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

    @Query("SELECT max(idInPlaylist) FROM playlist_song WHERE playlistId = :playlistId")
    suspend fun getPlaylistMaxId(playlistId: Int): Int?

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert
    suspend fun insertPlaylistSongs(playlistSong: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND idInPlaylist IN (:idsInPlaylist)")
    suspend fun deletePlaylistSongs(playlistId: Int, idsInPlaylist: List<Int>)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylistSongs(list: List<PlaylistSongEntity>)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)
}