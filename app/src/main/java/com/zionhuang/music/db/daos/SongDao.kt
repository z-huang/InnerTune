package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.extensions.toSQLiteQuery

const val QUERY_ALL_SONG = "SELECT * FROM song"
const val QUERY_ARTIST_SONG = "SELECT * FROM song WHERE artistId = %d"
const val QUERY_ORDER = " ORDER BY %s %s"

@Dao
interface SongDao {
    /**
     * Methods for the UI
     */
    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class])
    fun getSongsAsPagingSource(query: SupportSQLiteQuery): PagingSource<Int, Song>

    @Transaction
    @RawQuery
    suspend fun getSongsAsList(query: SupportSQLiteQuery): List<Song>

    /**
     * All Songs [PagingSource] with order [ORDER_CREATE_DATE], [ORDER_NAME], and [ORDER_ARTIST]
     */
    fun getAllSongsAsPagingSource(@SongSortType order: Int, descending: Boolean): PagingSource<Int, Song> =
        getSongsAsPagingSource((QUERY_ALL_SONG + getOrderQuery(order, descending)).toSQLiteQuery())

    suspend fun getAllSongsAsList(@SongSortType order: Int, descending: Boolean): List<Song> =
        getSongsAsList((QUERY_ALL_SONG + getOrderQuery(order, descending)).toSQLiteQuery())

    /**
     * Artist songs count
     */
    @Query("SELECT COUNT(songId) FROM song WHERE artistId = :artistId")
    suspend fun artistSongsCount(artistId: Int): Int

    /**
     * Artist Songs [PagingSource]
     */
    fun getArtistSongsAsPagingSource(artistId: Int, @SongSortType order: Int, descending: Boolean): PagingSource<Int, Song> =
        getSongsAsPagingSource(
            (QUERY_ARTIST_SONG.format(artistId) + getOrderQuery(
                order,
                descending
            )).toSQLiteQuery()
        )

    suspend fun getArtistSongsAsList(artistId: Int, @SongSortType order: Int, descending: Boolean) =
        getSongsAsList(
            (QUERY_ARTIST_SONG.format(artistId) + getOrderQuery(
                order,
                descending
            )).toSQLiteQuery()
        )

    /**
     * Playlist
     */
    @Transaction
    @Query(
        """
        SELECT song.*
          FROM playlist_song
               JOIN song
                 ON playlist_song.songId = song.songId
         WHERE playlistId = :playlistId
         ORDER BY playlist_song.idInPlaylist
        """
    )
    fun getPlaylistSongs(playlistId: Int): PagingSource<Int, Song>

    /**
     * Search
     */
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%'")
    fun searchSongs(query: String): PagingSource<Int, Song>

    /**
     * Internal methods
     */
    @Query("SELECT * FROM song WHERE songId = :songId")
    suspend fun getSongEntityById(songId: String): SongEntity?

    @Transaction
    @Query("SELECT * FROM song WHERE songId = :songId")
    suspend fun getSongById(songId: String): Song?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg songs: SongEntity)

    @Update
    suspend fun update(vararg songs: SongEntity)

    @Query("DELETE FROM song WHERE songId IN (:songsId)")
    suspend fun delete(songsId: List<String>)

    @Query("DELETE FROM song WHERE songId = :songId")
    suspend fun delete(songId: String)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE songId=:songId)")
    suspend fun contains(songId: String): Boolean

    fun getOrderQuery(@SongSortType order: Int, descending: Boolean) = QUERY_ORDER.format(
        when (order) {
            ORDER_CREATE_DATE -> "create_date"
            ORDER_NAME -> "title"
            ORDER_ARTIST -> "artistId"
            else -> throw IllegalArgumentException("Unexpected song sort type.")
        },
        if (descending) "DESC" else "ASC"
    )
}