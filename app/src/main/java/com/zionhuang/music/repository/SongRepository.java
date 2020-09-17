package com.zionhuang.music.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.zionhuang.music.db.MusicDatabase;
import com.zionhuang.music.db.SongDao;
import com.zionhuang.music.db.SongEntity;

import java.util.List;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class SongRepository {
    private static final String TAG = "SongRepository";
    private SongDao mSongDao;

    public SongRepository(Context context) {
        mSongDao = MusicDatabase.getInstance(context).getSongDao();
    }

    public LiveData<List<SongEntity>> getAllSongs() {
        return mSongDao.getAllSongs();
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
