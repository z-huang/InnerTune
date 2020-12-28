package com.zionhuang.music.db.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Transaction
    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsAsFlow(): Flow<List<Song>>

    @Transaction
    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsAsPagingSource(): PagingSource<Int, Song>

    @Transaction
    @Query("SELECT * FROM song WHERE download_state = $STATE_DOWNLOADING")
    fun getDownloadingSongsAsPagingSource(): PagingSource<Int, Song>

    @Transaction
    @Query("SELECT * FROM song WHERE artistId = :artistId")
    fun getArtistSongsAsPagingSource(artistId: Int): PagingSource<Int, Song>

    @Transaction
    @Query("SELECT * FROM song WHERE channelId = :channelId")
    fun getChannelSongsAsPagingSource(channelId: String): PagingSource<Int, Song>

    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongEntityById(songId: String): SongEntity?

    @Transaction
    @Query("SELECT * FROM song WHERE id = :songId")
    suspend fun getSongById(songId: String): Song?

    @Query("SELECT COUNT(id) FROM song WHERE channelId = :channelId")
    fun channelSongsCount(channelId: String): LiveData<Int>

    @Query("SELECT SUM(duration) FROM song WHERE channelId = :channelId")
    fun channelSongsDuration(channelId: String): LiveData<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg songs: SongEntity)

    fun insert(vararg songs: Song) = Unit

    @Update
    suspend fun update(vararg songs: SongEntity)

    @Delete
    suspend fun delete(vararg songs: SongEntity)

    @Query("DELETE FROM song WHERE id = :songId")
    suspend fun deleteById(songId: String)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    suspend fun contains(songId: String): Boolean
}