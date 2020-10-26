package com.zionhuang.music.db

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM song")
    fun getAllSongsAsLiveData(): LiveData<List<SongEntity>>

    @Query("SELECT * FROM song")
    fun getAllSongsAsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongById(songId: String): SongEntity?

    @Query("SELECT * FROM song WHERE id = :songId")
    fun getSongByIdAsSingle(songId: String): Single<SongEntity>

    @Query("SELECT * FROM song WHERE title=:title")
    fun getSongByTitle(title: String): Flowable<SongEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg songs: SongEntity): Completable

    @Delete
    fun delete(vararg songs: SongEntity)

    @Query("DELETE FROM song")
    fun deleteAll(): Completable

    @Update
    fun update(vararg songs: SongEntity)
}