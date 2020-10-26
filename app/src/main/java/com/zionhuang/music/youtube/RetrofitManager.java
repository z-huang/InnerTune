package com.zionhuang.music.youtube;

import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitManager {
    private static final String GoogleSuggestionAPIBaseUrl = "https://clients1.google.com/";
    private static volatile RetrofitManager mInstance;
    private SuggestionAPIService mSuggestionAPIService;

    private RetrofitManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GoogleSuggestionAPIBaseUrl)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .registerTypeAdapter(SuggestionResult.class, SuggestionResult.deserializer)
                        .create()))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        mSuggestionAPIService = retrofit.create(SuggestionAPIService.class);
    }

    public static RetrofitManager getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitManager.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitManager();
                }
            }
        }
        return mInstance;
    }

    public SuggestionAPIService getSuggestionAPIService() {
        return mSuggestionAPIService;
    }
}
