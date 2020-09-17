package com.zionhuang.music.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zionhuang.music.youtube.YouTubeRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class YouTubeSuggestionViewModel extends AndroidViewModel {
    private YouTubeRepository mYoutubeRepo;
    private MutableLiveData<String> query = new MutableLiveData<>();
    private MutableLiveData<List<String>> suggestions = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public YouTubeSuggestionViewModel(@NonNull Application application) {
        super(application);
        mYoutubeRepo = YouTubeRepository.getInstance(application);
    }

    public LiveData<String> getQuery() {
        return query;
    }

    public void setQuery(String q) {
        query.postValue(q);
    }

    public LiveData<List<String>> getSuggestions() {
        return suggestions;
    }

    public void fetchSuggestions(String query) {
        Disposable disposable = mYoutubeRepo.getSuggestions(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(suggestions::postValue, Throwable::printStackTrace);
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
