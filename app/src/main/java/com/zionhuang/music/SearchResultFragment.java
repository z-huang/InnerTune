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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchResultFragment extends Fragment {
    private MainActivity activity;
    private View root;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private String nextPageToken;
    private RequestQueue requestQueue;

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private ArrayList<Youtube.Item> mDataset;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title_tv;
            TextView description_tv;
            NetworkImageView thumbnail;

            ViewHolder(View v) {
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

        Adapter(ArrayList<Youtube.Item> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.title_tv.setText(mDataset.get(position).getTitle());
            holder.description_tv.setText(mDataset.get(position).getDescription());
            holder.thumbnail.setImageUrl(mDataset.get(position).getThumbnailURL(), NetworkManager.getInstance().getImageLoader());
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public void clear() {
            int size = getItemCount();
            mDataset.clear();
            notifyItemRangeRemoved(0, size);
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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String query = bundle.getString("query");
            Map<String, Object> map = new HashMap<>();
            map.put("key", "AIzaSyBwB96yiGC8EABKhBpZJpQyt5uIO4VqMwA");
            map.put("part", "snippet");
            map.put("safeSearch", "moderate");
            map.put("maxResults", 20);
            map.put("q", query);
            JSONObject parameter = new JSONObject(map);
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://www.googleapis.com/youtube/v3/search?key=AIzaSyBwB96yiGC8EABKhBpZJpQyt5uIO4VqMwA&part=snippet&safeSearch=moderate&maxResults=20&q=" + URLEncoder.encode(query, "UTF-8"), parameter, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("kind").equals("youtube#searchListResponse") && response.has("items")) {
                                nextPageToken = response.getString("nextPageToken");
                                JSONArray results = response.getJSONArray("items");
                                ArrayList<Youtube.Item> data = new ArrayList<>();
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject item = results.getJSONObject(i);
                                    String kind = item.getJSONObject("id").getString("kind");
                                    switch (kind) {
                                        case "youtube#video":
                                            data.add(new Youtube.Video(item));
                                            break;
                                        case "youtube#channel":
                                            data.add(new Youtube.Channel(item));
                                            break;
                                        case "youtube#playlist":
                                            data.add(new Youtube.Playlist(item));
                                            break;
                                    }
                                }
                                mAdapter = new Adapter(data);
                                recyclerView.setAdapter(mAdapter);
                                progressBar.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", "error: " + error.toString());
                    }
                });
                requestQueue.add(jsonObjectRequest);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    }
}
