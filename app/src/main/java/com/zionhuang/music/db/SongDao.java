package com.zionhuang.music.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface SongDao {
    @Query("SELECT * FROM song")
    LiveData<List<SongEntity>> getAllSongs();

    @Query("SELECT * FROM song WHERE id = :songId")
    Single<SongEntity> getSongById(String songId);

    @Query("SELECT * FROM song WHERE title=:title")
    Flowable<SongEntity> getSongByTitle(String title);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insert(SongEntity... songs);

    @Delete
    void delete(SongEntity... songs);

    @Query("DELETE FROM song")
    Completable deleteAll();

    @Update
    void update(SongEntity... songs);
}
