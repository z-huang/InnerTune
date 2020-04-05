package com.zionhuang.music.viewmodels;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Pair<String, String>> currentSong = new MutableLiveData<>();

    public MainViewModel() {
        currentSong.setValue(new Pair<String, String>("", ""));
    }

    public MutableLiveData<Pair<String, String>> getCurrentSong() {
        return currentSong;
    }
}
