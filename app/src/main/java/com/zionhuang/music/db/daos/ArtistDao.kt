package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.ArtistEntity

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist")
    fun getAllArtists(): List<ArtistEntity>

    @Query("SELECT * FROM artist")
    fun getAllArtistsAsPagingSource(): PagingSource<Int, ArtistEntity>

    @Query("SELECT * FROM artist WHERE id = :id")
    fun getArtist(id: Int): ArtistEntity?

    @Query("SELECT id FROM artist WHERE name = :name")
    fun getArtistId(name: String): Int?

    @Query("SELECT * FROM artist WHERE name LIKE '%' || :query || '%'")
    fun searchArtists(query: String): List<ArtistEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artist: ArtistEntity): Long

    @Query("DELETE FROM artist WHERE id = :artistId")
    suspend fun delete(artistId: Int)

    @Delete
    suspend fun delete(artist: ArtistEntity)
}