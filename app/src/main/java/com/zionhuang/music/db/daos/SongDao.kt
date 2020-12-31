package com.zionhuang.music.db.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.playback.queue.Queue
import com.zionhuang.music.ui.fragments.songs.ChannelSongsFragment
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    /**
     * Methods for the UI
     */

    /**
     * All Songs with order [Name, Artist, CreateDate]
     * @return [PagingSource]
     */
    @Transaction
    @Query("SELECT * FROM song ORDER BY title DESC")
    fun getAllSongsByName(): PagingSource<Int, Song>

    @Transaction
    @Query("SELECT * FROM song ORDER BY artistId DESC")
    fun getAllSongsByArtist(): PagingSource<Int, Song>

    @Transaction
    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsByCreateDate(): PagingSource<Int, Song>

    /**
     * Artist Songs
     * @return [PagingSource]
     */
    @Transaction
    @Query("SELECT * FROM song WHERE artistId = :artistId")
    fun getArtistSongsAsPagingSource(artistId: Int): PagingSource<Int, Song>

    /**
     * Channel Songs
     * @return [PagingSource]
     */
    @Transaction
    @Query("SELECT * FROM song WHERE channelId = :channelId")
    fun getChannelSongsAsPagingSource(channelId: String): PagingSource<Int, Song>

    /**
     * Methods for [ChannelSongsFragment]
     */
    @Query("SELECT COUNT(id) FROM song WHERE channelId = :channelId")
    fun channelSongsCount(channelId: String): LiveData<Int>

    @Query("SELECT SUM(duration) FROM song WHERE channelId = :channelId")
    fun channelSongsDuration(channelId: String): LiveData<Long?>

    /**
     * Methods for [Queue]
     */
    @Transaction
    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsAsFlow(): Flow<List<Song>>

    /**
     * Internal methods
     */
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongEntityById(songId: String): SongEntity?

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongById(songId: String): Song?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg songs: SongEntity)

    @Update
    suspend fun update(vararg songs: SongEntity)

    @Delete
    suspend fun delete(vararg songs: SongEntity)

    @Query("DELETE FROM song WHERE id = :songId")
    suspend fun deleteById(songId: String)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    suspend fun contains(songId: String): Boolean
}