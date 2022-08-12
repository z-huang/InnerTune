package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.SongArtistMap

@Dao
interface ArtistDao {
    @Transaction
    @Query("SELECT *, (SELECT COUNT(*) FROM song_artist_map WHERE artistId = artist.id) AS songCount FROM artist")
    fun getAllArtistsAsPagingSource(): PagingSource<Int, Artist>

    @Query("SELECT * FROM artist WHERE id = :id")
    suspend fun getArtistById(id: String): ArtistEntity?

    @Query("SELECT * FROM artist WHERE name = :name")
    suspend fun getArtistByName(name: String): ArtistEntity?

    @Query("SELECT * FROM artist WHERE name LIKE '%' || :query || '%'")
    suspend fun searchArtists(query: String): List<ArtistEntity>

    @Query("SELECT COUNT(*) FROM song_artist_map WHERE artistId = :id")
    suspend fun getArtistSongCount(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(songArtistMap: SongArtistMap): Long

    @Update
    suspend fun update(artist: ArtistEntity)

    @Delete
    suspend fun delete(artist: ArtistEntity)
}