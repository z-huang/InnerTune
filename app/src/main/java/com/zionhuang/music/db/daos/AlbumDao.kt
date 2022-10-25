package com.zionhuang.music.db.daos

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.AlbumArtistMap
import com.zionhuang.music.db.entities.AlbumEntity
import com.zionhuang.music.db.entities.SongAlbumMap
import com.zionhuang.music.extensions.toSQLiteQuery
import com.zionhuang.music.models.sortInfo.AlbumSortType
import com.zionhuang.music.models.sortInfo.ISortInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Transaction
    @RawQuery(observedEntities = [AlbumEntity::class, AlbumArtistMap::class])
    fun getAlbumsAsFlow(query: SupportSQLiteQuery): Flow<List<Album>>

    fun getAllAlbumsAsFlow(sortInfo: ISortInfo<AlbumSortType>) = getAlbumsAsFlow((QUERY_ALL_ALBUM + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM album")
    suspend fun getAlbumCount(): Int

    @Transaction
    @Query("SELECT * FROM album WHERE title LIKE '%' || :query || '%'")
    fun searchAlbums(query: String): Flow<List<Album>>

    @Transaction
    @Query("SELECT * FROM album WHERE title LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchAlbumsPreview(query: String, previewSize: Int): Flow<List<Album>>

    @Query("SELECT * FROM album WHERE id = :id")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(albumArtistMap: AlbumArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songAlbumMaps: List<SongAlbumMap>): List<Long>

    @Update
    suspend fun update(album: AlbumEntity)

    @Update
    suspend fun update(songAlbumMaps: List<SongAlbumMap>)

    suspend fun upsert(songAlbumMaps: List<SongAlbumMap>) {
        insert(songAlbumMaps)
            .withIndex()
            .mapNotNull { if (it.value == -1L) songAlbumMaps[it.index] else null }
            .let { update(it) }
    }

    @Delete
    suspend fun delete(album: AlbumEntity)

    fun getSortQuery(sortInfo: ISortInfo<AlbumSortType>) = QUERY_ORDER.format(
        when (sortInfo.type) {
            AlbumSortType.CREATE_DATE -> "rowid"
            AlbumSortType.NAME -> "album.title"
            AlbumSortType.ARTIST -> throw IllegalArgumentException("Unexpected album sort type.")
            AlbumSortType.YEAR -> "album.year"
            AlbumSortType.SONG_COUNT -> "album.songCount"
            AlbumSortType.LENGTH -> "album.duration"
        },
        if (sortInfo.isDescending) "DESC" else "ASC"
    )

    companion object {
        private const val QUERY_ALL_ALBUM = "SELECT * FROM album"
        private const val QUERY_ORDER = " ORDER BY %s %s"
    }
}