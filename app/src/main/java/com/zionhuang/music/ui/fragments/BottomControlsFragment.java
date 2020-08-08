package com.zionhuang.music.ui.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.zionhuang.music.R;
import com.zionhuang.music.ui.activities.MainActivity;
import com.zionhuang.music.ui.widgets.BottomSheetListener;
import com.zionhuang.music.ui.widgets.MediaWidgetsController;
import com.zionhuang.music.viewmodels.PlaybackViewModel;

import static com.zionhuang.music.utils.Utils.makeTimeString;

public class BottomControlsFragment extends BaseFragment implements BottomSheetListener {
    private static final String TAG = "BottomControlsFragment";
    private PlaybackViewModel mPlaybackViewModel;

    private MotionLayout mMotionLayout;

    private TextView mBtmSongTitle;
    private TextView mBtmSongArtist;
    private TextView mSongTitle;
    private TextView mSongArtist;
    private ProgressBar mProgressBar;
    private SeekBar mSeekBar;
    private TextView mProgressText;
    private TextView mDurationText;
    private MediaWidgetsController mMediaWidgetsController;
    private ImageView mBottomBarPlayPauseBtn;
    private ImageView mPlayPauseBtn;

    @Override
    protected int layoutId() {
        return R.layout.bottom_controls_sheet;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlaybackViewModel = new ViewModelProvider(requireActivity()).get(PlaybackViewModel.class);

        mBottomBarPlayPauseBtn = findViewById(R.id.btn_btm_play_pause);
        mPlayPauseBtn = findViewById(R.id.btn_play_pause);

        mBtmSongTitle = findViewById(R.id.tv_btm_song_title);
        mBtmSongArtist = findViewById(R.id.tv_btm_song_artist);
        mSongTitle = findViewById(R.id.tv_song_title);
        mSongArtist = findViewById(R.id.tv_song_artist);
        mBtmSongTitle.setSelected(true);
        mBtmSongArtist.setSelected(true);
        mSongTitle.setSelected(true);
        mSongArtist.setSelected(true);

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressText = findViewById(R.id.tv_position);
        mDurationText = findViewById(R.id.tv_duration);
        mSeekBar = findViewById(R.id.seek_bar);
        mMotionLayout = findViewById(R.id.bottom_controls_motion_layout);
        mMotionLayout.setTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                mProgressBar.setVisibility(View.GONE);
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
        });

        mMediaWidgetsController = new MediaWidgetsController(requireContext(), mProgressBar, mSeekBar, mProgressText, mPlayPauseBtn, mBottomBarPlayPauseBtn);
        ((MainActivity) requireActivity()).addBottomSheetListener(this);

        mPlaybackViewModel.getCurrentData().observe(getViewLifecycleOwner(), mediaData -> {
            mBtmSongTitle.setText(mediaData.getTitle());
            mSongTitle.setText(mediaData.getTitle());
            mBtmSongArtist.setText(mediaData.getArtist());
            mSongArtist.setText(mediaData.getArtist());
            mDurationText.setText(makeTimeString(mediaData.getDuration() / 1000));
        });

        PlayerView playerView = findViewById(R.id.player_view);
        mPlaybackViewModel.setPlayerView(playerView);
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
        switch (newState) {
            case BottomSheetBehavior.STATE_EXPANDED:
            case BottomSheetBehavior.STATE_DRAGGING:
            case BottomSheetBehavior.STATE_HALF_EXPANDED:
            case BottomSheetBehavior.STATE_HIDDEN:
            case BottomSheetBehavior.STATE_SETTLING:
                break;
            case BottomSheetBehavior.STATE_COLLAPSED:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        mMotionLayout.setProgress(slideOffset);
    }
}
