package com.zionhuang.music;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.NetworkImageView;

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
    private Adapter mAdapter;
    private ArrayList<Youtube.Item.Base> dataSet;
    private RequestQueue requestQueue;
    private String nextPageToken;
    private String query;

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<Youtube.Item.Base> mDataset;

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView title_tv;
            TextView description_tv;
            NetworkImageView thumbnail;

            ItemViewHolder(View v) {
                super(v);
                title_tv = v.findViewById(R.id.title_text_view);
                description_tv = v.findViewById(R.id.description_text_view);
                thumbnail = v.findViewById(R.id.thumbnail_view);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("SuggestFragment", "Element " + getAdapterPosition() + " clicked.");
                    }
                });

            }
        }

        class LoadingViewHolder extends RecyclerView.ViewHolder {
            LoadingViewHolder(View v) {
                super(v);
            }
        }

        Adapter(ArrayList<Youtube.Item.Base> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
                return new ItemViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_list_item, parent, false);
                return new LoadingViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Youtube.Item.Base item = mDataset.get(position);
            if (holder instanceof ItemViewHolder) {
                ((ItemViewHolder) holder).title_tv.setText(((Youtube.Item.ItemBase) item).getTitle());
                ((ItemViewHolder) holder).description_tv.setText(((Youtube.Item.ItemBase) item).getDescription());
                ((ItemViewHolder) holder).thumbnail.setImageUrl(((Youtube.Item.ItemBase) item).getThumbnailURL(), NetworkManager.getInstance().getImageLoader());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mDataset.get(position) instanceof Youtube.Item.ItemBase) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_result, container, false);

        activity = (MainActivity) getActivity();
        activity.currentFragment = this;

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
        mAdapter = new Adapter(dataSet);
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
