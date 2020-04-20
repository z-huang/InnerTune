package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.R;
import com.zionhuang.music.adapters.SearchSuggestionAdapter;
import com.zionhuang.music.viewmodels.MainViewModel;
import com.zionhuang.music.viewmodels.SearchViewModel;

import java.util.ArrayList;

public class SearchSuggestionFragment extends BaseFragment {
    private static final String TAG = "SearchSuggestionFragment";
    private MainViewModel mMainViewModel;
    private SearchViewModel mViewModel;
    private SearchSuggestionAdapter mAdapter;

    @Override
    protected int layoutId() {
        return R.layout.fragment_search_suggestion;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.suggestion_recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<String> dataSet = new ArrayList<>();
        mAdapter = new SearchSuggestionAdapter(dataSet, new ViewModelProvider(requireActivity()).get(MainViewModel.class));
        recyclerView.setAdapter(mAdapter);

        mMainViewModel.getSearchQuery().observe(getViewLifecycleOwner(), this::fetchSuggestion);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String query = bundle.getString("query");
            fetchSuggestion(query);
        }
    }

    private void fetchSuggestion(final String query) {
        if (query == null || query.isEmpty()) {
            mAdapter.clear();
            return;
        }
        mViewModel.fetchSuggestions(query).observe(getViewLifecycleOwner(), result -> mAdapter.setData(result));
    }
}
