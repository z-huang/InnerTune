package com.zionhuang.music.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.api.services.youtube.model.SearchResult;
import com.zionhuang.music.youtube.YouTubeDataSource;
import com.zionhuang.music.youtube.YouTubeRepository;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import kotlinx.coroutines.CoroutineScope;

public class SearchViewModel extends AndroidViewModel {
    private static final String TAG = "SearchResultVM";

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);

    private YouTubeRepository mYoutubeRepo;
    private MutableLiveData<List<SearchResult>> searchLiveData = new MutableLiveData<>();

    public SearchViewModel(@NonNull Application application) {
        super(application);
        mYoutubeRepo = YouTubeRepository.getInstance(application);
    }

    public Flowable<PagingData<SearchResult>> search(String query) {
        Pager<String, SearchResult> pager = new Pager<>(
                new PagingConfig(20),
                () -> new YouTubeDataSource.Search(mYoutubeRepo, query)
        );
        Flowable<PagingData<SearchResult>> flowable = PagingRx.getFlowable(pager);
        PagingRx.cachedIn(flowable, viewModelScope);
        return flowable;
    }

    @Override
    protected void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }
}
