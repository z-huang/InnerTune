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
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SearchSuggestionFragment extends Fragment {
    private MainActivity activity;
    private View root;
    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    RequestQueue requestQueue;

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private ArrayList<String> mDataset;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View v) {
                super(v);
                textView = v.findViewById(R.id.textView);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("SuggestFragment", "Element " + getAdapterPosition() + " clicked.");
                    }
                });
                v.findViewById(R.id.fill_text_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.fillSearchBarQuery(textView.getText().toString());
                    }
                });
            }
        }

        Adapter(ArrayList<String> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_list_item, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(mDataset.get(position));
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
        root = inflater.inflate(R.layout.fragment_search_suggestion, container, false);
        activity = (MainActivity) getActivity();
        activity.currentFragment = this;
        Cache cache = new DiskBasedCache(getContext().getCacheDir(), 1024 * 1024); // 1MB cap
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
        recyclerView = root.findViewById(R.id.suggestion_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        return root;
    }

    public void onQueryTextChange(final String text) {
        Log.d("SearchSuggestion", "start request");
        if (text.isEmpty()) {
            mAdapter.clear();
        }
        try {
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, "https://clients1.google.com/complete/search?client=firefox&ds=yt&q=" + URLEncoder.encode(text, "UTF-8"), new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        if (response.length() == 2 && text.equals(response.get(0))) {
                            JSONArray suggestions = response.getJSONArray(1);
                            ArrayList<String> data = new ArrayList<>();
                            for (int i=0; i<suggestions.length(); i++) {
                                data.add(suggestions.getString(i));
                            }
                            mAdapter = new Adapter(data);
                            recyclerView.setAdapter(mAdapter);
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
            requestQueue.add(jsonArrayRequest);
        } catch (UnsupportedEncodingException e) {
            Log.e("SuggestionFragment", "Encoding error: "+e.toString());
        }

    }
}
