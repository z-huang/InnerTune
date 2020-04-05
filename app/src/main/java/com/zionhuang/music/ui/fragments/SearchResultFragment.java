package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.zionhuang.music.utils.EndlessScrollListener;
import com.zionhuang.music.utils.NetworkManager;
import com.zionhuang.music.R;
import com.zionhuang.music.utils.Youtube;
import com.zionhuang.music.adapters.SearchResultsAdapter;
import com.zionhuang.music.ui.activities.MainActivity;
import com.zionhuang.music.viewmodels.MainViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultFragment extends Fragment {
    private MainActivity activity;
    private View root;
    private ProgressBar progressBar;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private SearchResultsAdapter mAdapter;
    private ArrayList<Youtube.Item.Base> dataSet;
    private RequestQueue requestQueue;
    private String nextPageToken;
    private String query;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_result, container, false);

        activity = (MainActivity) getActivity();

        progressBar = root.findViewById(R.id.progressBar);

        requestQueue = NetworkManager.getInstance().getRequestQueue();

        recyclerView = root.findViewById(R.id.result_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public boolean hasMore() {
                return nextPageToken != null;
            }

            @Override
            public void loadMore() {
                Youtube.Request.Search(requestQueue, new Youtube.Request.Parameter().setPageToken(nextPageToken).setQuery(query), new Youtube.Request.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("kind").equals("youtube#searchListResponse") && response.has("items")) {
                                nextPageToken = response.has("nextPageToken") ? response.getString("nextPageToken") : null;
                                dataSet.remove(dataSet.size() - 1);
                                mAdapter.notifyItemRemoved(dataSet.size());
                                int originalItems = mAdapter.getItemCount();
                                JSONArray results = response.getJSONArray("items");
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject item = results.getJSONObject(i);
                                    String kind = item.getJSONObject("id").getString("kind");
                                    switch (kind) {
                                        case "youtube#video":
                                            dataSet.add(new Youtube.Item.Video(item));
                                            break;
                                        case "youtube#channel":
                                            dataSet.add(new Youtube.Item.Channel(item));
                                            break;
                                        case "youtube#playlist":
                                            dataSet.add(new Youtube.Item.Playlist(item));
                                            break;
                                    }
                                }
                                int length = results.length();
                                if (nextPageToken != null) {
                                    dataSet.add(new Youtube.Item.Loader());
                                    length++;
                                }
                                mAdapter.notifyItemRangeInserted(originalItems, length);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
        });
        dataSet = new ArrayList<>();
        mAdapter = new SearchResultsAdapter(dataSet, getContext(), new ViewModelProvider(activity).get(MainViewModel.class));
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            query = bundle.getString("query");
            Youtube.Request.Search(requestQueue, new Youtube.Request.Parameter().setQuery(query), new Youtube.Request.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getString("kind").equals("youtube#searchListResponse") && response.has("items")) {
                            nextPageToken = response.has("nextPageToken") ? response.getString("nextPageToken") : null;
                            JSONArray results = response.getJSONArray("items");
                            dataSet.clear();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject item = results.getJSONObject(i);
                                String kind = item.getJSONObject("id").getString("kind");
                                switch (kind) {
                                    case "youtube#video":
                                        dataSet.add(new Youtube.Item.Video(item));
                                        break;
                                    case "youtube#channel":
                                        dataSet.add(new Youtube.Item.Channel(item));
                                        break;
                                    case "youtube#playlist":
                                        dataSet.add(new Youtube.Item.Playlist(item));
                                        break;
                                }
                            }
                            if (nextPageToken != null) {
                                dataSet.add(new Youtube.Item.Loader());
                            }
                            mAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.d("Response", "error: " + e.toString());
                }
            });
        } else {
            Log.d("SearchResult", "no query");
        }
    }
}
