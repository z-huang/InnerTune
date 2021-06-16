package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistSongEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylistsAsPagingSource(): PagingSource<Int, PlaylistEntity>

    @Query("SELECT * FROM playlist")
    suspend fun getAllPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId ORDER BY idInPlaylist")
    suspend fun getPlaylistSongEntities(playlistId: Int): List<PlaylistSongEntity>

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

    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND idInPlaylist = :idInPlaylist")
    suspend fun removeSong(playlistId: Int, idInPlaylist: Int)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)
}