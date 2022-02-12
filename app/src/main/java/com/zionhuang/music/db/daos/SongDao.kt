package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.models.base.ISortInfo
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.extensions.toSQLiteQuery

@Dao
interface SongDao {
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSong(songId: String): Song?

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Transaction
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%'")
    fun searchSongsAsPagingSource(query: String): PagingSource<Int, Song>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songs: List<SongEntity>)

    @Update
    suspend fun update(songs: List<SongEntity>)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    suspend fun contains(songId: String): Boolean

    @Query("DELETE FROM song WHERE id IN (:songIds)")
    suspend fun delete(songIds: List<String>)


    @Transaction
    @RawQuery
    suspend fun getSongsAsList(query: SupportSQLiteQuery): List<Song>

    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class])
    fun getSongsAsPagingSource(query: SupportSQLiteQuery): PagingSource<Int, Song>

    suspend fun getAllSongsAsList(sortInfo: ISortInfo): List<Song> = getSongsAsList((QUERY_ALL_SONG + getSortQuery(sortInfo)).toSQLiteQuery())
    fun getAllSongsAsPagingSource(sortInfo: ISortInfo): PagingSource<Int, Song> = getSongsAsPagingSource((QUERY_ALL_SONG + getSortQuery(sortInfo)).toSQLiteQuery())
    suspend fun getArtistSongsAsList(artistId: Int, sortInfo: ISortInfo): List<Song> = getSongsAsList((QUERY_ARTIST_SONG.format(artistId) + getSortQuery(sortInfo)).toSQLiteQuery())
    fun getArtistSongsAsPagingSource(artistId: Int, sortInfo: ISortInfo): PagingSource<Int, Song> = getSongsAsPagingSource((QUERY_ARTIST_SONG.format(artistId) + getSortQuery(sortInfo)).toSQLiteQuery())

    @Transaction
    @Query(QUERY_PLAYLIST_SONGS)
    fun getPlaylistSongsAsPagingSource(playlistId: Int): PagingSource<Int, Song>

    @Transaction
    @Query(QUERY_PLAYLIST_SONGS)
    suspend fun getPlaylistSongsAsList(playlistId: Int): List<Song>


    @Query("SELECT COUNT(id) FROM song WHERE artistId = :artistId")
    suspend fun artistSongsCount(artistId: Int): Int

    fun getSortQuery(sortInfo: ISortInfo) = QUERY_ORDER.format(
        when (sortInfo.type) {
            ORDER_CREATE_DATE -> "create_date"
            ORDER_NAME -> "title"
            ORDER_ARTIST -> "artistId"
            else -> throw IllegalArgumentException("Unexpected song sort type.")
        },
        if (sortInfo.isDescending) "DESC" else "ASC"
    )

    companion object {
        private const val QUERY_ALL_SONG = "SELECT * FROM song"
        private const val QUERY_ARTIST_SONG = "SELECT * FROM song WHERE artistId = %d"
        private const val QUERY_ORDER = " ORDER BY %s %s"
        private const val QUERY_PLAYLIST_SONGS =
            """
            SELECT song.*, playlist_song.idInPlaylist
              FROM playlist_song
                   JOIN song
                     ON playlist_song.songId = song.id
             WHERE playlistId = :playlistId
             ORDER BY playlist_song.idInPlaylist
            """
    }
}