package com.zionhuang.music.db.daos

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
    @Query(QUERY_ALL_PLAYLIST)
    suspend fun getAllPlaylistsAsList(): List<Playlist>

    @Transaction
    @RawQuery(observedEntities = [PlaylistEntity::class, PlaylistSongMap::class])
    fun getPlaylistsAsFlow(query: SupportSQLiteQuery): Flow<List<Playlist>>

    fun getAllPlaylistsAsFlow(sortInfo: ISortInfo<PlaylistSortType>): Flow<List<Playlist>> = getPlaylistsAsFlow((QUERY_ALL_PLAYLIST + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM playlist")
    suspend fun getPlaylistCount(): Int

    @Transaction
    @Query("$QUERY_ALL_PLAYLIST WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): Playlist

    @Transaction
    @Query("$QUERY_ALL_PLAYLIST WHERE name LIKE '%' || :query || '%'")
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    @Transaction
    @Query("$QUERY_ALL_PLAYLIST WHERE name LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchPlaylistsPreview(query: String, previewSize: Int): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId AND position = :position")
    suspend fun getPlaylistSongMap(playlistId: String, position: Int): PlaylistSongMap?

    @Query("SELECT * FROM playlist_song_map WHERE songId IN (:songIds)")
    suspend fun getPlaylistSongMaps(songIds: List<String>): List<PlaylistSongMap>

    @Query("SELECT * FROM playlist_song_map WHERE playlistId = :playlistId AND position >= :from ORDER BY position")
    suspend fun getPlaylistSongMaps(playlistId: String, from: Int): List<PlaylistSongMap>

    @Query("UPDATE playlist_song_map SET position = position - 1 WHERE playlistId = :playlistId AND :from <= position")
    suspend fun decrementSongPositions(playlistId: String, from: Int)

    @Query("UPDATE playlist_song_map SET position = position - 1 WHERE playlistId = :playlistId AND :from <= position AND position <= :to")
    suspend fun decrementSongPositions(playlistId: String, from: Int, to: Int)

    @Query("UPDATE playlist_song_map SET position = position + 1 WHERE playlistId = :playlistId AND :from <= position AND position <= :to")
    suspend fun incrementSongPositions(playlistId: String, from: Int, to: Int)

    suspend fun renewSongPositions(playlistId: String, from: Int) {
        val maps = getPlaylistSongMaps(playlistId, from)
        if (maps.isEmpty()) return
        var position = if (from <= 0) 0 else maps[0].position
        update(maps.map { it.copy(position = position++) })
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(playlist: PlaylistEntity): Long

    @Insert
    suspend fun insert(playlistSongMaps: List<PlaylistSongMap>)

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Update
    suspend fun update(playlistSongMap: PlaylistSongMap)

    @Update
    suspend fun update(playlistSongMaps: List<PlaylistSongMap>)

    @Delete
    suspend fun delete(playlists: List<PlaylistEntity>)


    suspend fun deletePlaylistSong(playlistId: String, position: Int) = deletePlaylistSong(playlistId, listOf(position))

    @Query("DELETE FROM playlist_song_map WHERE playlistId = :playlistId AND position IN (:position)")
    suspend fun deletePlaylistSong(playlistId: String, position: List<Int>)

    @Query("SELECT max(position) FROM playlist_song_map WHERE playlistId = :playlistId")
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