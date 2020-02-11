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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchSuggestion extends Fragment {
    private View root;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private String[] mDataset;

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

                    }
                });
            }
        }

        Adapter(String[] myDataset) {
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
            holder.textView.setText(mDataset[position]);
        }

        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }

    public SearchSuggestion() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_suggestion, container, false);
        recyclerView = root.findViewById(R.id.suggestion_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        String[] data = {"a", "b", "c"};
        mAdapter = new Adapter(data);
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    public void onQueryTextChange(String text) {
        Log.d("SearchSuggestion", "start request");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "https://clients1.google.com/complete/search?client=youtube&hl=zh-TW&gl=tw&gs_ri=youtube&ds=yt&callback=g&q=ehp", (String) null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response", "error");
            }
        });
    }
}
