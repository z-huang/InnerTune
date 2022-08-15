package com.zionhuang.music.db.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.toSQLiteQuery
import com.zionhuang.music.models.base.ISortInfo

@Dao
interface SongDao {
    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSong(songId: String): Song?

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%' AND NOT isTrash")
    fun searchSongsAsPagingSource(query: String): PagingSource<Int, Song>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: SongEntity)

    @Update
    suspend fun update(song: SongEntity)

    @Update
    suspend fun update(songs: List<SongEntity>)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    suspend fun hasSong(songId: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    fun hasSongLiveData(songId: String): LiveData<Boolean>

    @Delete
    suspend fun delete(songs: List<SongEntity>)

    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class, AlbumEntity::class, SongArtistMap::class, SongAlbumMap::class])
    suspend fun getSongsAsList(query: SupportSQLiteQuery): List<Song>

    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class, AlbumEntity::class, SongArtistMap::class, SongAlbumMap::class])
    fun getSongsAsPagingSource(query: SupportSQLiteQuery): PagingSource<Int, Song>

    suspend fun getAllSongsAsList(sortInfo: ISortInfo): List<Song> =
        getSongsAsList((QUERY_ALL_SONG + getSortQuery(sortInfo)).toSQLiteQuery())

    fun getAllSongsAsPagingSource(sortInfo: ISortInfo): PagingSource<Int, Song> =
        getSongsAsPagingSource((QUERY_ALL_SONG + getSortQuery(sortInfo)).toSQLiteQuery())

    suspend fun getArtistSongsAsList(artistId: String, sortInfo: ISortInfo): List<Song> =
        getSongsAsList((QUERY_ARTIST_SONG.format(artistId) + getSortQuery(sortInfo)).toSQLiteQuery())

    fun getArtistSongsAsPagingSource(artistId: String, sortInfo: ISortInfo): PagingSource<Int, Song> =
        getSongsAsPagingSource((QUERY_ARTIST_SONG.format(artistId) + getSortQuery(sortInfo)).toSQLiteQuery())

    @Transaction
    @Query(QUERY_PLAYLIST_SONGS)
    fun getPlaylistSongsAsPagingSource(playlistId: String): PagingSource<Int, Song>

    @Transaction
    @Query(QUERY_PLAYLIST_SONGS)
    suspend fun getPlaylistSongsAsList(playlistId: String): List<Song>

    fun getSortQuery(sortInfo: ISortInfo) = QUERY_ORDER.format(
        when (sortInfo.type) {
            ORDER_CREATE_DATE -> "song.create_date"
            ORDER_NAME -> "song.title"
            else -> throw IllegalArgumentException("Unexpected song sort type.")
        },
        if (sortInfo.isDescending) "DESC" else "ASC"
    )

    companion object {
        private const val QUERY_ALL_SONG = "SELECT * FROM song WHERE NOT isTrash"
        private const val QUERY_ARTIST_SONG =
            """
            SELECT song.*
              FROM song_artist_map
                   JOIN song
                     ON song_artist_map.songId = song.id
             WHERE artistId = "%s" AND NOT song.isTrash
            """
        private const val QUERY_ORDER = " ORDER BY %s %s"
        private const val QUERY_PLAYLIST_SONGS =
            """
            SELECT song.*, playlist_song_map.idInPlaylist
              FROM playlist_song_map
                   JOIN song
                     ON playlist_song_map.songId = song.id
             WHERE playlistId = :playlistId AND NOT song.isTrash
             ORDER BY playlist_song_map.idInPlaylist
            """
    }
}