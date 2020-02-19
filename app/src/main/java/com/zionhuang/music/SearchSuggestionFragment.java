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

import com.android.volley.RequestQueue;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SearchSuggestionFragment extends Fragment {
    private MainActivity activity;
    private View root;
    private RecyclerView recyclerView;
    private Adapter mAdapter;
    private ArrayList<String> dataSet;
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
                        activity.search(textView.getText().toString());
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

        requestQueue = NetworkManager.getInstance().getRequestQueue();

        recyclerView = root.findViewById(R.id.suggestion_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        dataSet = new ArrayList<>();
        mAdapter = new Adapter(dataSet);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String query = bundle.getString("query");
            onQueryTextChange(query);
        }
    }

    public void onQueryTextChange(final String query) {
        if (query == null || query.isEmpty()) {
            mAdapter.clear();
            return;
        }
        Youtube.Request.Suggest(requestQueue, query, new Youtube.Request.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    if (response.length() == 2 && query.equals(response.get(0))) {
                        JSONArray suggestions = response.getJSONArray(1);
                        dataSet.clear();
                        for (int i = 0; i < suggestions.length(); i++) {
                            dataSet.add(suggestions.getString(i));
                        }
                        mAdapter.notifyDataSetChanged();
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
    }
}
