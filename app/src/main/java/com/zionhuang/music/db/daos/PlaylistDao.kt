package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.extensions.toSQLiteQuery
import com.zionhuang.music.models.sortInfo.ISortInfo
import com.zionhuang.music.models.sortInfo.PlaylistSortType
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist")
    suspend fun getAllPlaylistsAsList(): List<Playlist>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist")
    fun getAllPlaylistsAsPagingSource(): PagingSource<Int, Playlist>

    @Transaction
    @RawQuery(observedEntities = [PlaylistEntity::class, PlaylistSongMap::class])
    fun getPlaylistsAsFlow(query: SupportSQLiteQuery): Flow<List<Playlist>>

    fun getAllPlaylistsAsFlow(sortInfo: ISortInfo<PlaylistSortType>): Flow<List<Playlist>> = getPlaylistsAsFlow((QUERY_ALL_PLAYLIST + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM playlist")
    suspend fun getPlaylistCount(): Int

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    suspend fun getPlaylist(playlistId: String): PlaylistEntity?

    @Query("SELECT * FROM playlist WHERE name LIKE '%' || :query || '%'")
    suspend fun searchPlaylists(query: String): List<PlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Insert
    suspend fun insert(playlistSongMaps: List<PlaylistSongMap>)

    @Update
    suspend fun update(playlist: PlaylistEntity)

    suspend fun upsert(playlist: PlaylistEntity) {
        if (insert(playlist) == -1L) {
            update(playlist)
        }
    }

    @Delete
    suspend fun delete(playlists: List<PlaylistEntity>)

    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId ORDER BY idInPlaylist")
    suspend fun getPlaylistSongEntities(playlistId: String): List<PlaylistSongMap>

    @Update
    suspend fun updatePlaylistSongEntities(list: List<PlaylistSongMap>)

    @Insert
    suspend fun insertPlaylistSongEntities(playlistSong: List<PlaylistSongMap>)

    @Query("DELETE FROM playlist_song_map WHERE playlistId = :playlistId AND idInPlaylist = :idInPlaylist")
    suspend fun deletePlaylistSongEntities(playlistId: String, idInPlaylist: List<Int>)

    @Query("SELECT max(idInPlaylist) FROM playlist_song_map WHERE playlistId = :playlistId")
    suspend fun getPlaylistMaxId(playlistId: String): Int?

    fun getSortQuery(sortInfo: ISortInfo<PlaylistSortType>) = QUERY_ORDER.format(
        when (sortInfo.type) {
            PlaylistSortType.CREATE_DATE -> "rowid"
            PlaylistSortType.NAME -> "playlist.name"
            PlaylistSortType.SONG_COUNT -> throw IllegalArgumentException("Unexpected playlist sort type.")
        },
        if (sortInfo.isDescending) "DESC" else "ASC"
    )

    companion object {
        private const val QUERY_ALL_PLAYLIST = "SELECT *, (SELECT COUNT(*) FROM playlist_song_map WHERE playlistId = playlist.id) AS songCount FROM playlist"
        private const val QUERY_ORDER = " ORDER BY %s %s"
    }
}