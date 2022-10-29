package com.zionhuang.music.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.toSQLiteQuery
import com.zionhuang.music.models.sortInfo.ISortInfo
import com.zionhuang.music.models.sortInfo.SongSortType
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class, AlbumEntity::class, SongArtistMap::class, SongAlbumMap::class])
    suspend fun getSongsAsList(query: SupportSQLiteQuery): List<Song>

    @Transaction
    @RawQuery(observedEntities = [SongEntity::class, ArtistEntity::class, AlbumEntity::class, SongArtistMap::class, SongAlbumMap::class])
    fun getSongsAsFlow(query: SupportSQLiteQuery): Flow<List<Song>>

    fun getAllSongsAsFlow(sortInfo: ISortInfo<SongSortType>): Flow<List<Song>> = getSongsAsFlow((QUERY_ALL_SONG + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM song WHERE NOT isTrash")
    suspend fun getSongCount(): Int

    suspend fun getArtistSongsAsList(artistId: String, sortInfo: ISortInfo<SongSortType>): List<Song> = getSongsAsList((QUERY_ARTIST_SONG.format(artistId) + getSortQuery(sortInfo)).toSQLiteQuery())
    fun getArtistSongsAsFlow(artistId: String, sortInfo: ISortInfo<SongSortType>) = getSongsAsFlow((QUERY_ARTIST_SONG.format(artistId) + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM song_artist_map WHERE artistId = :artistId")
    suspend fun getArtistSongCount(artistId: String): Int

    @Query("SELECT song.id FROM song_artist_map JOIN song ON song_artist_map.songId = song.id WHERE artistId = :artistId AND NOT song.isTrash LIMIT 5")
    suspend fun getArtistSongsPreview(artistId: String): List<String>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT song.* FROM song JOIN song_album_map ON song.id = song_album_map.songId WHERE song_album_map.albumId = :albumId")
    suspend fun getAlbumSongs(albumId: String): List<Song>

    @Transaction
    @Query(QUERY_PLAYLIST_SONGS)
    suspend fun getPlaylistSongsAsList(playlistId: String): List<Song>

    @Transaction
    @Query(QUERY_PLAYLIST_SONGS)
    fun getPlaylistSongsAsFlow(playlistId: String): Flow<List<Song>>

    fun getLikedSongs(sortInfo: ISortInfo<SongSortType>) = getSongsAsFlow((QUERY_LIKED_SONG + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM song WHERE liked")
    fun getLikedSongCount(): Flow<Int>

    fun getDownloadedSongsAsFlow(sortInfo: ISortInfo<SongSortType>) = getSongsAsFlow((QUERY_DOWNLOADED_SONG + getSortQuery(sortInfo)).toSQLiteQuery())

    suspend fun getDownloadedSongsAsList(sortInfo: ISortInfo<SongSortType>) = getSongsAsList((QUERY_DOWNLOADED_SONG + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM song WHERE download_state = $STATE_DOWNLOADED")
    fun getDownloadedSongCount(): Flow<Int>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSong(songId: String?): Song?

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongAsLiveData(songId: String?): LiveData<Song?>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongAsFlow(songId: String?): Flow<Song?>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%' AND NOT isTrash")
    fun searchSongs(query: String): Flow<List<Song>>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE download_state = $STATE_DOWNLOADED AND title LIKE '%' || :query || '%'")
    fun searchDownloadedSongs(query: String): Flow<List<Song>>

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM song WHERE title LIKE '%' || :query || '%' AND NOT isTrash LIMIT :previewSize")
    fun searchSongsPreview(query: String, previewSize: Int): Flow<List<Song>>

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id = :songId)")
    suspend fun hasSong(songId: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id = :songId)")
    fun hasSongAsLiveData(songId: String): LiveData<Boolean>

    @Query("UPDATE song SET totalPlayTime = totalPlayTime + :playTime WHERE id = :songId")
    suspend fun incrementSongTotalPlayTime(songId: String, playTime: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(song: SongEntity)

    @Update
    suspend fun update(song: SongEntity)

    @Update
    suspend fun update(songs: List<SongEntity>)

    @Delete
    suspend fun delete(songs: List<SongEntity>)

    fun getSortQuery(sortInfo: ISortInfo<SongSortType>) = QUERY_ORDER.format(
        when (sortInfo.type) {
            SongSortType.CREATE_DATE -> "song.create_date"
            SongSortType.NAME -> "song.title"
            SongSortType.PLAY_TIME -> "song.totalPlayTime"
            else -> throw IllegalArgumentException("Unexpected song sort type.")
        },
        if (sortInfo.isDescending) "DESC" else "ASC"
    )

    companion object {
        private const val QUERY_ORDER = " ORDER BY %s %s"
        private const val QUERY_ALL_SONG = "SELECT * FROM song WHERE NOT isTrash"
        private const val QUERY_ARTIST_SONG =
            """
            SELECT song.*
              FROM song_artist_map
                   JOIN song
                     ON song_artist_map.songId = song.id
             WHERE artistId = "%s" AND NOT song.isTrash
            """
        private const val QUERY_PLAYLIST_SONGS =
            """
            SELECT song.*, playlist_song_map.position
              FROM playlist_song_map
                   JOIN song
                     ON playlist_song_map.songId = song.id
             WHERE playlistId = :playlistId AND NOT song.isTrash
             ORDER BY playlist_song_map.position
            """
        private const val QUERY_LIKED_SONG = "SELECT * FROM song WHERE liked"
        private const val QUERY_DOWNLOADED_SONG = "SELECT * FROM song WHERE download_state = $STATE_DOWNLOADED"
    }
}