package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zionhuang.music.R;
import com.zionhuang.music.databinding.BottomControlsSheetBinding;
import com.zionhuang.music.ui.activities.MainActivity;
import com.zionhuang.music.ui.widgets.BottomSheetListener;
import com.zionhuang.music.ui.widgets.MediaWidgetsController;
import com.zionhuang.music.viewmodels.PlaybackViewModel;

public class BottomControlsFragment extends BaseFragment implements BottomSheetListener, MotionLayout.TransitionListener {
    private static final String TAG = "BottomControlsFragment";
    private BottomControlsSheetBinding binding;
    private PlaybackViewModel mPlaybackViewModel;

    private MotionLayout mMotionLayout;
    private ProgressBar mProgressBar;

    private MediaWidgetsController mMediaWidgetsController;

    @Override
    protected int layoutId() {
        return R.layout.bottom_controls_sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomControlsSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlaybackViewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);
        mPlaybackViewModel.setPlayerView(findViewById(R.id.player_view));

        binding.setViewModel(mPlaybackViewModel);
        binding.setLifecycleOwner(this);

        setupUI();
    }

    private void setupUI() {
        // Marquee
        findViewById(R.id.tv_btm_song_title).setSelected(true);
        findViewById(R.id.tv_btm_song_artist).setSelected(true);
        findViewById(R.id.tv_item_title).setSelected(true);
        findViewById(R.id.tv_item_channel).setSelected(true);

        mMotionLayout = findViewById(R.id.bottom_controls_motion_layout);
        mMotionLayout.addTransitionListener(this);
        mProgressBar = findViewById(R.id.progress_bar);
        ((MainActivity) requireActivity()).setBottomSheetListener(this);

        mMediaWidgetsController = new MediaWidgetsController(requireContext(), mProgressBar, findViewById(R.id.seek_bar), findViewById(R.id.tv_position));
    }

    @Override
    public void onResume() {
        super.onResume();
        mPlaybackViewModel.getMediaController().observe(getViewLifecycleOwner(), mediaController -> mMediaWidgetsController.setMediaController(mediaController));
    }

    @Override
    public void onStop() {
        mMediaWidgetsController.disconnectController();
        super.onStop();
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        mMotionLayout.setProgress(slideOffset);
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

    }

    @Override
    public void onTransitionCompleted(MotionLayout motionLayout, int i) {

    }

    @Override
    public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

    }
}
