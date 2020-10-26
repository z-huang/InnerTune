package com.zionhuang.music.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.zionhuang.music.db.MusicDatabase;
import com.zionhuang.music.db.SongDao;
import com.zionhuang.music.db.SongEntity;

import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;
import kotlinx.coroutines.flow.Flow;

public class SongRepository {
    private static final String TAG = "SongRepository";
    private SongDao mSongDao;

    public SongRepository(Context context) {
        mSongDao = MusicDatabase.getInstance(context).getSongDao();
    }

    public LiveData<List<SongEntity>> getAllSongsAsLiveData() {
        return mSongDao.getAllSongsAsLiveData();
    }

    public Flow<List<SongEntity>> getAllSongsAsFlow() {
        return mSongDao.getAllSongsAsFlow();
    }

    public SongEntity getSongById(String id) {
        return mSongDao.getSongById(id);
    }

    public void insert(SongEntity song) {
        mSongDao.insert(song)
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public void deleteAll() {
        mSongDao.deleteAll()
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
