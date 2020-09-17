package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.youtube.model.SearchResult;
import com.zionhuang.music.R;
import com.zionhuang.music.ui.adapters.SearchResultAdapter;
import com.zionhuang.music.ui.widgets.RecyclerViewClickManager;
import com.zionhuang.music.viewmodels.PlaybackViewModel;
import com.zionhuang.music.viewmodels.SearchViewModel;

import java.util.Objects;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;

import static autodispose2.AutoDispose.autoDisposable;

public class SearchResultFragment extends BaseFragment {
    private static final String TAG = "SearchResultFragment";
    private PlaybackViewModel mPlaybackViewModel;

    private RecyclerView mRecyclerView;
    private SearchResultAdapter mAdapter;

    private ProgressBar mProgressBar;

    private String mQuery;

    @Override
    protected int layoutId() {
        return R.layout.fragment_search_result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchViewModel mSearchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        mPlaybackViewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        mProgressBar = findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());

        mRecyclerView = findViewById(R.id.result_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new SearchResultAdapter();
        mRecyclerView.setAdapter(mAdapter);
        RecyclerViewClickManager.setup(mRecyclerView, (i, v) -> {
            SearchResult item = mAdapter.getItemByPosition(i);
            Bundle bundle = new Bundle();
            bundle.putString("title", item.getSnippet().getTitle());
            bundle.putString("artist", item.getSnippet().getChannelTitle());
            mPlaybackViewModel.playMedia(item.getId().getVideoId(), bundle);
        }, null);

        mQuery = SearchResultFragmentArgs.fromBundle(requireArguments()).getSearchQuery();
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setTitle(mQuery);
        mSearchViewModel.getFlowableByQuery(Objects.requireNonNull(mQuery))
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(pagingData -> mAdapter.submitData(getLifecycle(), pagingData));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_icon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_searchResultFragment_to_searchSuggestionFragment);
        }
        return true;
    }
}
