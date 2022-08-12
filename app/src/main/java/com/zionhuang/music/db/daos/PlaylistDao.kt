package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistSongMap

@Dao
interface PlaylistDao {
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist")
    suspend fun getAllPlaylistsAsList(): List<Playlist>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist")
    fun getAllPlaylistsAsPagingSource(): PagingSource<Int, Playlist>

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: String): PlaylistEntity?

    @Query("SELECT * FROM playlist WHERE name LIKE '%' || :query || '%'")
    suspend fun searchPlaylists(query: String): List<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: PlaylistEntity)

    @Insert
    suspend fun insert(playlistSongMaps: List<PlaylistSongMap>)

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)


    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId ORDER BY idInPlaylist")
    suspend fun getPlaylistSongEntities(playlistId: String): List<PlaylistSongMap>

    @Update
    suspend fun updatePlaylistSongEntities(list: List<PlaylistSongMap>)

    @Insert
    suspend fun insertPlaylistSongEntities(playlistSong: List<PlaylistSongMap>)

    @Query("DELETE FROM playlist_song_map WHERE playlistId = :playlistId AND idInPlaylist = (:idInPlaylist)")
    suspend fun deletePlaylistSongEntities(playlistId: String, idInPlaylist: List<Int>)

    @Query("SELECT max(idInPlaylist) FROM playlist_song_map WHERE playlistId = :playlistId")
    suspend fun getPlaylistMaxId(playlistId: String): Int?
}