package com.zionhuang.music.youtube;

import android.content.Context;
import android.util.Log;

import com.google.api.services.youtube.model.SearchListResponse;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class YouTubeRepository {
    private static final String TAG = "YoutubeRepository";

    private static volatile YouTubeRepository mInstance;
    private YouTubeAPIService mYouTubeAPIService;
    private SuggestionAPIService mSuggestionAPIService;

    private YouTubeRepository(Context context) {
        mYouTubeAPIService = new YouTubeAPIService(context);
        mSuggestionAPIService = RetrofitManager.getInstance().getSuggestionAPIService();
    }

    public static YouTubeRepository getInstance(Context context) {
        if (mInstance == null) {
            synchronized (YouTubeRepository.class) {
                if (mInstance == null) {
                    mInstance = new YouTubeRepository(context);
                }
            }
        }
        return mInstance;
    }

    public Observable<List<String>> getSuggestions(String query) {
        return mSuggestionAPIService.suggest(query)
                .map(SuggestionResult::getSuggestions);
    }

    public Single<SearchListResponse> search(String query) {
        return search(query, null);
    }

    public Single<SearchListResponse> search(String query, String pageToken) {
        Log.d(TAG, "search: " + query + ", pageToken: " + pageToken);
        return mYouTubeAPIService.search(query, pageToken);
    }
}
