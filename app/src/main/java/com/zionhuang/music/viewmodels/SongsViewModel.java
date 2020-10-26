package com.zionhuang.music.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.zionhuang.music.db.SongEntity;
import com.zionhuang.music.repository.SongRepository;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SongsViewModel extends AndroidViewModel {
    private static final String TAG = "SongsViewModel";
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private SongRepository songRepository;
    private LiveData<List<SongEntity>> allSongs;

    public SongsViewModel(@NonNull Application application) {
        super(application);
        songRepository = new SongRepository(application);
        allSongs = songRepository.getAllSongsAsLiveData();
    }

    public LiveData<List<SongEntity>> getAllSongs() {
        return allSongs;
    }

    public void insertSong(SongEntity song) {
        songRepository.insert(song);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
