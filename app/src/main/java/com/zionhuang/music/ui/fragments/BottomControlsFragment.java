package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        binding.btmSongTitle.setSelected(true);
        binding.btmSongArtist.setSelected(true);
        binding.songTitle.setSelected(true);
        binding.songArtist.setSelected(true);

        binding.motionLayout.addTransitionListener(this);
        ((MainActivity) requireActivity()).setBottomSheetListener(this);

        mMediaWidgetsController = new MediaWidgetsController(requireContext(), binding.progressBar, binding.seekBar, binding.positionText);
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
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        if (slideOffset < 0) slideOffset = 0;
        if (slideOffset > 1) slideOffset = 1;
        binding.motionLayout.setProgress(slideOffset);
    }

    @Override
    public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
        binding.progressBar.setVisibility(View.INVISIBLE);
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
