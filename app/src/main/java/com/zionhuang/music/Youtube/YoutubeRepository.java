package com.zionhuang.music.Youtube;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zionhuang.music.utils.NetworkManager;

import java.util.ArrayList;
import java.util.List;

public class YoutubeRepository {
    private static final String TAG = "YoutubeRepository";

    private static volatile YoutubeRepository mInstance;
    private RequestQueue mRequestQueue;
    private final Gson gson = new GsonBuilder().registerTypeAdapter(YtItem.BaseItem.class, new YtItem.YtItemDeserializer()).create();

    private YoutubeRepository(Context context) {
        mRequestQueue = NetworkManager.getInstance(context).getRequestQueue();
    }

    public static YoutubeRepository getInstance(Context context) {
        if (mInstance == null) {
            synchronized (YoutubeRepository.class) {
                if (mInstance == null) {
                    mInstance = new YoutubeRepository(context);
                }
            }
        }
        return mInstance;
    }

    public LiveData<List<String>> fetchSuggestions(String query) {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        YtRequest.suggest(mRequestQueue, query, new YtRequest.Listener<JsonArray>() {
            @Override
            public void onResponse(JsonArray response) {
                JsonArray suggestions = response.get(1).getAsJsonArray();
                ArrayList<String> dataSet = new ArrayList<>();
                for (JsonElement jsonElement : suggestions) {
                    dataSet.add(jsonElement.getAsString());
                }
                result.postValue(dataSet);
            }

            @Override
            public void onError(Exception e) {
                result.postValue(new ArrayList<>());
            }
        });
        return result;
    }

    public LiveData<YtResult> search(String query) {
        return search(query, null);
    }

    public LiveData<YtResult> search(String query, String pageToken) {
        MutableLiveData<YtResult> result = new MutableLiveData<>();
        YtRequest.Parameter parameter = new YtRequest.Parameter().setQuery(query);
        if (pageToken != null) {
            parameter.setPageToken(pageToken);
        }
        YtRequest.search(mRequestQueue, parameter, new YtRequest.Listener<JsonObject>() {
            @Override
            public void onResponse(JsonObject response) {
                String nextPageToken = response.has("nextPageToken") ? response.get("nextPageToken").getAsString() : null;
                JsonArray results = response.has("items") ? response.get("items").getAsJsonArray() : null;
                if (results == null) {
                    result.postValue(new YtResult(null, null));
                } else {
                    ArrayList<YtItem.BaseItem> dataSet = new ArrayList<>();
                    for (JsonElement jsonElement : results) {
                        dataSet.add(gson.fromJson(jsonElement, YtItem.BaseItem.class));
                    }
                    result.postValue(new YtResult(dataSet, nextPageToken));
                }
            }

            @Override
            public void onError(Exception e) {
                result.postValue(new YtResult(null, null));
            }
        });
        return result;
    }
}
