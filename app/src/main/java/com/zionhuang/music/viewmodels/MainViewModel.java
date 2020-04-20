package com.zionhuang.music.viewmodels;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Pair<String, String>> currentSong = new MutableLiveData<>();
    private MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private MutableLiveData<Boolean> querySubmitBool = new MutableLiveData<>();

    public MainViewModel() {
        currentSong.setValue(new Pair<String, String>("", ""));
    }

    public LiveData<Pair<String, String>> getCurrentSong() {
        return currentSong;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<Boolean> getQuerySubmitListener() {
        return querySubmitBool;
    }

    public void setCurrentSong(Pair<String, String> song) {
        currentSong.setValue(song);
    }

    public void setSearchQuery(String query, boolean submit) {
        searchQuery.setValue(query);
        if (submit) {
            querySubmitBool.setValue(true);
        }
    }
}
