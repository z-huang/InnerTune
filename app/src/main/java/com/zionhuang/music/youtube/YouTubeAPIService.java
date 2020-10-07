package com.zionhuang.music.youtube;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.VideoListResponse;
import com.zionhuang.music.R;

import io.reactivex.rxjava3.core.Single;

public class YouTubeAPIService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "YoutubeAPIService";
    private final String API_KEY_RID;
    private static String API_KEY;
    private static final String REGION_CODE = "US";
    private static final String CATEGORY_MUSIC = "10";
    private YouTube mYouTube;

    public YouTubeAPIService(Context context) {
        mYouTube = new YouTube.Builder(new NetHttpTransport.Builder().build(), new GsonFactory(), null)
                .setApplicationName(context.getResources().getString(R.string.app_name))
                .build();
        API_KEY_RID = context.getString(R.string.api_key);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        API_KEY = sharedPreferences.getString(API_KEY_RID, "");
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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

    public Single<VideoListResponse> popularMusic(String pageToken) {
        return Single.fromCallable(() ->
                mYouTube.videos().list("snippet,contentDetails,statistics")
                        .setKey(API_KEY)
                        .setChart("mostPopular")
                        .setVideoCategoryId(CATEGORY_MUSIC)
                        .setRegionCode(REGION_CODE)
                        .setPageToken(pageToken)
                        .setMaxResults(20L)
                        .execute());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(API_KEY_RID)) {
            API_KEY = sharedPreferences.getString(key, "");
        }
    }
}
