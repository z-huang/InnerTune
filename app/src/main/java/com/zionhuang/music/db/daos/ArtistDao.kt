package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.ArtistEntity

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist")
    suspend fun getAllArtistsAsList(): List<ArtistEntity>

    @Query("SELECT * FROM artist")
    fun getAllArtistsAsPagingSource(): PagingSource<Int, ArtistEntity>

    @Query("SELECT * FROM artist WHERE id = :id")
    suspend fun getArtist(id: Int): ArtistEntity?

    @Query("SELECT id FROM artist WHERE name = :name")
    suspend fun getArtistId(name: String): Int?

    @Query("SELECT * FROM artist WHERE name LIKE '%' || :query || '%'")
    suspend fun searchArtists(query: String): List<ArtistEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(artist: ArtistEntity): Long

    @Update
    suspend fun update(artist: ArtistEntity)

    @Delete
    suspend fun delete(artist: ArtistEntity)
}