package com.zionhuang.music.db

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
interface SongDao {
    @Query("SELECT * FROM song ORDER BY create_date")
    fun getAllSongsAsLiveData(): LiveData<List<SongEntity>>

    @Query("SELECT * FROM song ORDER BY create_date")
    fun getAllSongsAsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM song ORDER BY create_date")
    fun getAllSongsAsPagingSource(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM song WHERE download_state=$STATE_DOWNLOADING")
    fun getDownloadingSongsAsPagingSource(): PagingSource<Int, SongEntity>

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongById(songId: String): SongEntity?

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongByIdAsFlow(songId: String): Flow<SongEntity>

    fun getSongByIdDistinctUntilChanged(songId: String) = getSongByIdAsFlow(songId).distinctUntilChanged()

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongByIdAsSingle(songId: String): Single<SongEntity>

    @Query("SELECT * FROM song WHERE title=:title")
    fun getSongByTitle(title: String): Flowable<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg songs: SongEntity)

    @Delete
    fun delete(vararg songs: SongEntity)

    @Query("DELETE FROM song")
    fun deleteAll()

    @Update
    fun update(vararg songs: SongEntity)

    @Query("SELECT EXISTS (SELECT 1 FROM song WHERE id=:songId)")
    fun contains(songId: String): Boolean
}