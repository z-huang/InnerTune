package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zionhuang.music.R;
import com.zionhuang.music.models.SongParcel;
import com.zionhuang.music.ui.adapters.SongsAdapter;
import com.zionhuang.music.ui.widgets.RecyclerViewClickManager;
import com.zionhuang.music.viewmodels.PlaybackViewModel;
import com.zionhuang.music.viewmodels.SongsViewModel;

import static com.zionhuang.music.playback.queue.Queue.QUEUE_ALL_SONG;

public class SongsFragment extends BaseFragment {

    @Override
    protected int layoutId() {
        return R.layout.layout_recyclerview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SongsViewModel songsViewModel = new ViewModelProvider(this).get(SongsViewModel.class);
        PlaybackViewModel playbackViewModel = new ViewModelProvider(this).get(PlaybackViewModel.class);

        SongsAdapter songsAdapter = new SongsAdapter();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(songsAdapter);
        RecyclerViewClickManager.setup(recyclerView,
                (i, v) -> playbackViewModel.playMedia(SongParcel.fromSongEntity(songsAdapter.getSongFromPosition(i)), QUEUE_ALL_SONG),
                null);

        songsViewModel.getAllSongs().observe(getViewLifecycleOwner(), songsAdapter::setDataSet);
    }
}
