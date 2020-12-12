package com.zionhuang.music.db.daos

import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface SongDao {

    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsAsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM song ORDER BY create_date DESC")
    fun getAllSongsAsPagingSource(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM song WHERE download_state=$STATE_DOWNLOADING")
    fun getDownloadingSongsAsPagingSource(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongById(songId: String): SongEntity?

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongByIdAsFlow(songId: String): Flow<SongEntity>

    fun getSongByIdAsFlowDistinct(songId: String) = getSongByIdAsFlow(songId).distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg songs: SongEntity)

    @Update
    suspend fun update(vararg songs: SongEntity)

    @Delete
    suspend fun delete(vararg songs: SongEntity)

}