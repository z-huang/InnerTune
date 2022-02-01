package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistSongEntity

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    suspend fun getAllPlaylistsAsList(): List<PlaylistEntity>

    @Query("SELECT * FROM playlist")
    fun getAllPlaylistsAsPagingSource(): PagingSource<Int, PlaylistEntity>

    @Query("SELECT * FROM playlist WHERE playlistId = :id")
    suspend fun getPlaylist(id: Int): PlaylistEntity?

    @Query("SELECT * FROM playlist WHERE name LIKE '%' || :query || '%'")
    fun searchPlaylists(query: String): List<PlaylistEntity>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)


    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId ORDER BY idInPlaylist")
    suspend fun getPlaylistSongEntities(playlistId: Int): List<PlaylistSongEntity>

    @Update
    suspend fun updatePlaylistSongEntities(list: List<PlaylistSongEntity>)

    @Insert
    suspend fun insertPlaylistSongEntities(playlistSong: List<PlaylistSongEntity>)

    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND idInPlaylist = (:idInPlaylist)")
    suspend fun deletePlaylistSongEntities(playlistId: Int, idInPlaylist: List<Int>)

    @Query("SELECT max(idInPlaylist) FROM playlist_song WHERE playlistId = :playlistId")
    suspend fun getPlaylistMaxId(playlistId: Int): Int?
}