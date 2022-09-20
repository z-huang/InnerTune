package com.zionhuang.music.db.daos

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.SongArtistMap
import com.zionhuang.music.extensions.toSQLiteQuery
import com.zionhuang.music.models.sortInfo.ArtistSortType
import com.zionhuang.music.models.sortInfo.ISortInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Transaction
    @RawQuery(observedEntities = [ArtistEntity::class, SongArtistMap::class])
    fun getArtistsAsFlow(query: SupportSQLiteQuery): Flow<List<Artist>>

    fun getAllArtistsAsFlow(sortInfo: ISortInfo<ArtistSortType>) = getArtistsAsFlow((QUERY_ALL_ARTIST + getSortQuery(sortInfo)).toSQLiteQuery())

    @Query("SELECT COUNT(*) FROM artist")
    suspend fun getArtistCount(): Int

    @Query("SELECT * FROM artist WHERE id = :id")
    suspend fun getArtistById(id: String): ArtistEntity?

    @Query("SELECT * FROM artist WHERE name = :name")
    suspend fun getArtistByName(name: String): ArtistEntity?

    @Query("SELECT COUNT(*) FROM song_artist_map WHERE artistId = :id")
    suspend fun getArtistSongCount(id: String): Int

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist WHERE name LIKE '%' || :query || '%'")
    fun searchArtists(query: String): Flow<List<Artist>>

    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist WHERE name LIKE '%' || :query || '%' LIMIT :previewSize")
    fun searchArtistsPreview(query: String, previewSize: Int): Flow<List<Artist>>

    @Query("SELECT EXISTS(SELECT * FROM artist WHERE id = :id)")
    suspend fun hasArtist(id: String): Boolean

    @Query("DELETE FROM song_artist_map WHERE songId = :songId")
    suspend fun deleteSongArtists(songId: String)

    suspend fun deleteSongArtists(songIds: List<String>) = songIds.forEach {
        deleteSongArtists(it)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songArtistMap: SongArtistMap): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songArtistMaps: List<SongArtistMap>)

    @Update
    suspend fun update(artist: ArtistEntity)

    @Delete
    suspend fun delete(artists: List<ArtistEntity>)

    fun getSortQuery(sortInfo: ISortInfo<ArtistSortType>) = QUERY_ORDER.format(
        when (sortInfo.type) {
            ArtistSortType.CREATE_DATE -> "rowid"
            ArtistSortType.NAME -> "artist.name"
            else -> throw IllegalArgumentException("Unexpected artist sort type.")
        },
        if (sortInfo.isDescending) "DESC" else "ASC"
    )

    companion object {
        private const val QUERY_ALL_ARTIST = "SELECT *, (SELECT COUNT(*) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist"
        private const val QUERY_ORDER = " ORDER BY %s %s"
    }
}