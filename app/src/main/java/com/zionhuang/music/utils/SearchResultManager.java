package com.zionhuang.music.utils;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zionhuang.music.Youtube.YtItem;
import com.zionhuang.music.Youtube.YtRequest;

import java.util.ArrayList;

public class SearchResultManager {
    private final String TAG = "SearchResultManager";
    private SearchResultManagerFactory mSearchResultManagerFactory;
    private RequestQueue mRequestQueue;
    private ArrayList<YtItem.Base> mDataSet;
    private String currentQuery;
    private String nextPageToken;

    private final Gson gson = new GsonBuilder().registerTypeAdapter(YtItem.BaseItem.class, new YtItem.YtItemDeserializer()).create();

    public SearchResultManager(SearchResultManagerFactory searchResultManagerFactory, ArrayList<YtItem.Base> dataSet, RequestQueue requestQueue) {
        mSearchResultManagerFactory = searchResultManagerFactory;
        mDataSet = dataSet;
        mRequestQueue = requestQueue;
    }

    public void searchQuery(String query) {
        currentQuery = query;
        YtRequest.search(mRequestQueue, new YtRequest.Parameter().setQuery(currentQuery), new YtRequest.Listener<JsonObject>() {
            @Override
            public void onResponse(JsonObject response) {
                nextPageToken = response.has("nextPageToken") ? response.get("nextPageToken").getAsString() : null;
                JsonArray results = response.has("items") ? response.get("items").getAsJsonArray() : null;
                mDataSet.clear();
                if (results == null) {
                    return;
                }
                for (JsonElement jsonElement : results) {
                    mDataSet.add(gson.fromJson(jsonElement, YtItem.BaseItem.class));
                }
                if (nextPageToken != null) {
                    mDataSet.add(new YtItem.Loader());
                }
                mSearchResultManagerFactory.onDataSetChanged();
                mSearchResultManagerFactory.showRecyclerView();
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "search query error: " + e.toString());
            }
        });
    }

    public interface SearchResultManagerFactory {
        void showRecyclerView();

        void onDataSetChanged();

        void onItemRemoved(int position);

        void onItemRangeInserted(int positionStart, int itemCount);
    }
}
