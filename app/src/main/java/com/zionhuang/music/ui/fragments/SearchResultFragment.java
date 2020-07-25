package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.R;
import com.zionhuang.music.adapters.SearchResultAdapter;
import com.zionhuang.music.ui.recycler.RecyclerPaddingDecoration;
import com.zionhuang.music.utils.EndlessScrollListener;
import com.zionhuang.music.viewmodels.PlaybackViewModel;
import com.zionhuang.music.viewmodels.SearchViewModel;
import com.zionhuang.music.youtube.YtItem;
import com.zionhuang.music.youtube.YtResult;

import java.util.ArrayList;

public class SearchResultFragment extends BaseFragment implements SearchResultAdapter.InteractionListener {
    private static final String TAG = "SearchResultFragment";
    private SearchViewModel mSearchViewModel;
    private PlaybackViewModel mPlaybackViewModel;

    private RecyclerView mRecyclerView;
    private SearchResultAdapter mAdapter;
    private ArrayList<YtItem.Base> mDataSet;

    private ProgressBar mProgressBar;

    private boolean firstSearch = true;
    private String mQuery;
    private String mNextPageToken;

    @Override
    protected int layoutId() {
        return R.layout.fragment_search_result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        mPlaybackViewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        mProgressBar = findViewById(R.id.progressBar);

        mDataSet = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        mRecyclerView = findViewById(R.id.result_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new RecyclerPaddingDecoration(0, 48, 0, 48));
        mRecyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public boolean hasMore() {
                return mNextPageToken != null;
            }

            @Override
            public void loadMore() {
                mSearchViewModel.search(mQuery, mNextPageToken).observe(getViewLifecycleOwner(), SearchResultFragment.this::applyResults);
            }
        });

        mAdapter = new SearchResultAdapter(mDataSet, mSearchViewModel.getSelection(), getViewLifecycleOwner(), getContext());
        mAdapter.setInteractionListener(this);
        mRecyclerView.setAdapter(mAdapter);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mQuery = bundle.getString("query");
            mSearchViewModel.search(mQuery, null).observe(getViewLifecycleOwner(), this::applyResults);
        } else {
            Log.d(TAG, "no query");
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "on hidden changed");
        if (!hidden) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                mQuery = bundle.getString("query");
                mSearchViewModel.search(mQuery, null).observe(getViewLifecycleOwner(), this::applyResults);
            } else {
                Log.d(TAG, "no query");
            }
        } else {
            hideRecyclerView();
        }
    }

    private void applyResults(YtResult result) {
        if (mDataSet.size() > 0 && mDataSet.get(mDataSet.size() - 1) instanceof YtItem.Loader) {
            mDataSet.remove(mDataSet.size() - 1);
            mAdapter.notifyItemRemoved(mDataSet.size());
        }
        if (result.list == null) {
            return;
        }
        int originalSize = mDataSet.size();
        mDataSet.addAll(result.list);
        mAdapter.notifyItemRangeInserted(originalSize, result.list.size());
        mNextPageToken = result.nextPageToken;
        if (mNextPageToken != null) {
            mDataSet.add(new YtItem.Loader());
            mAdapter.notifyItemInserted(mDataSet.size() - 1);
        }
        if (firstSearch) {
            showRecyclerView();
            firstSearch = false;
        }
    }

    private void showRecyclerView() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideRecyclerView() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClicked(YtItem.BaseItem item) {
        Bundle bundle = new Bundle();
        bundle.putString("title", item.getTitle());
        bundle.putString("artist", item.getDescription());
        mPlaybackViewModel.playMedia(item.getId(), bundle);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
