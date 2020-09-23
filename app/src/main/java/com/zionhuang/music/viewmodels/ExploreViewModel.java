package com.zionhuang.music.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.api.services.youtube.model.Video;
import com.zionhuang.music.youtube.YouTubeDataSource;
import com.zionhuang.music.youtube.YouTubeRepository;

import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;

public class ExploreViewModel extends AndroidViewModel {
    private CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);

    private YouTubeRepository mYouTubeRepo;
    private Flowable<PagingData<Video>> flowable;

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        mYouTubeRepo = YouTubeRepository.getInstance(application);
        Pager<String, Video> pager = new Pager<>(
                new PagingConfig(20),
                () -> new YouTubeDataSource.Popular(mYouTubeRepo)
        );
        flowable = PagingRx.getFlowable(pager);
        PagingRx.cachedIn(flowable, viewModelScope);
    }

    public Flowable<PagingData<Video>> getFlowable() {
        return flowable;
    }
}