package com.zionhuang.music.youtube;

import androidx.paging.rxjava3.RxPagingSource;

import com.google.api.services.youtube.model.SearchResult;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class YouTubeDataSource extends RxPagingSource<String, SearchResult> {
    private YouTubeRepository mYouTubeRepo;
    private String mQuery;

    public YouTubeDataSource(YouTubeRepository youTubeRepository, String query) {
        mYouTubeRepo = youTubeRepository;
        mQuery = query;
    }

    @NotNull
    @Override
    public Single<LoadResult<String, SearchResult>> loadSingle(@NotNull LoadParams<String> loadParams) {
        String pageToken = loadParams.getKey();
        return mYouTubeRepo.search(mQuery, pageToken)
                .subscribeOn(Schedulers.io())
                .map(res -> (LoadResult<String, SearchResult>) new LoadResult.Page<>(res.getItems(), res.getPrevPageToken(), res.getNextPageToken()))
                .onErrorReturn(LoadResult.Error::new);
    }
}
