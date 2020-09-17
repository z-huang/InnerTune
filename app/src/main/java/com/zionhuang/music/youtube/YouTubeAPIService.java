package com.zionhuang.music.youtube;

import android.content.Context;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.zionhuang.music.R;

import io.reactivex.rxjava3.core.Single;

public class YouTubeAPIService {
    private static final String TAG = "YoutubeAPIService";
    private static final String API_KEY = "API_KEY";
    private YouTube mYouTube;

    public YouTubeAPIService(Context context) {
        mYouTube = new YouTube.Builder(new NetHttpTransport.Builder().build(), new GsonFactory(), null)
                .setApplicationName(context.getResources().getString(R.string.app_name))
                .build();
    }

    public Single<SearchListResponse> search(String query, String pageToken) {
        return Single.fromCallable(() ->
                mYouTube.search().list("snippet")
                        .setKey(API_KEY)
                        .setQ(query)
                        .setPageToken(pageToken)
                        .setMaxResults(20L)
                        .execute());
    }
}
