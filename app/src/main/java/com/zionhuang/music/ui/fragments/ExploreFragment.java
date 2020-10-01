package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.youtube.model.Video;
import com.zionhuang.music.R;
import com.zionhuang.music.models.SongParcel;
import com.zionhuang.music.ui.adapters.ExploreAdapter;
import com.zionhuang.music.ui.adapters.LoadStateAdapter;
import com.zionhuang.music.ui.widgets.RecyclerViewClickManager;
import com.zionhuang.music.viewmodels.ExploreViewModel;
import com.zionhuang.music.viewmodels.PlaybackViewModel;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;

import static autodispose2.AutoDispose.autoDisposable;

public class ExploreFragment extends BaseFragment {
    private static final String TAG = "ExplorationFragment";

    @Override
    protected int layoutId() {
        return R.layout.fragment_explore;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ExploreViewModel mViewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        PlaybackViewModel playbackViewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        RecyclerView recyclerView = findViewById(R.id.explore_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ExploreAdapter adapter = new ExploreAdapter();

        ProgressBar progressBar = findViewById(R.id.progress_bar);
        Button retryBtn = findViewById(R.id.btn_retry);
        TextView errorMsgTextView = findViewById(R.id.tv_error_msg);

        adapter.addLoadStateListener(loadState -> {
            progressBar.setVisibility(loadState.getSource().getRefresh() instanceof LoadState.Loading ? View.VISIBLE : View.GONE);
            retryBtn.setVisibility(loadState.getSource().getRefresh() instanceof LoadState.Error ? View.VISIBLE : View.GONE);
            loadState.getSource().getAppend();
            String errorMsg = loadState.getSource().getRefresh() instanceof LoadState.Error ? ((LoadState.Error) loadState.getSource().getRefresh()).getError().getLocalizedMessage() : "";
            errorMsgTextView.setText(errorMsg);
            return null;
        });
        retryBtn.setOnClickListener(v -> adapter.retry());

        recyclerView.setAdapter(adapter.withLoadStateFooter(new LoadStateAdapter(adapter::retry)));
        RecyclerViewClickManager.setup(recyclerView, (i, v) -> {
            Video video = adapter.getItemByPosition(i);
            playbackViewModel.playMedia(SongParcel.fromVideo(video));
        }, null);

        mViewModel.getFlowable()
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe(pagingData -> adapter.submitData(getLifecycle(), pagingData));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search_icon, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            NavHostFragment.findNavController(this).navigate(R.id.action_explorationFragment_to_searchSuggestionFragment);
        }
        return true;
    }
}