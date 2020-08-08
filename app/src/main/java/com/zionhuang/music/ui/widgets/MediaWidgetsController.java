package com.zionhuang.music.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.provider.Settings;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zionhuang.music.R;

import static com.zionhuang.music.utils.Utils.makeTimeString;

public class MediaWidgetsController {
    private static final String TAG = "MediaWidgetsController";
    private ProgressBar mProgressBar;
    private SeekBar mSeekBar;
    private boolean mSeekBarIsTracking;
    private TextView mProgressTextView;
    private ImageView mBottomBarPlayPauseBtn;
    private ImageView mPlayPauseBtn;

    private MediaControllerCompat mMediaController;
    private ControllerCallback mControllerCallback;
    private ValueAnimator mProgressAnimator;
    private long mDuration;
    private float mDurationScale;

    public MediaWidgetsController(Context context, ProgressBar progressBar, SeekBar seekBar, TextView progressTextView, ImageView playPauseBtn, ImageView bottomBarPlayPauseBtn) {
        mProgressBar = progressBar;
        mSeekBar = seekBar;
        mProgressTextView = progressTextView;
        mPlayPauseBtn = playPauseBtn;
        mBottomBarPlayPauseBtn = bottomBarPlayPauseBtn;
        mDurationScale = Settings.Global.getFloat(context.getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgressTextView.setText(makeTimeString(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBarIsTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaController != null) {
                    mMediaController.getTransportControls().seekTo(mSeekBar.getProgress());
                }
                mSeekBarIsTracking = false;
            }
        });
        View.OnClickListener playPauseOnClickListener = v -> {
            if (mMediaController != null) {
                if (mMediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    mMediaController.getTransportControls().pause();
                } else {
                    mMediaController.getTransportControls().play();
                }
            }
        };
        mPlayPauseBtn.setOnClickListener(playPauseOnClickListener);
        mBottomBarPlayPauseBtn.setOnClickListener(playPauseOnClickListener);
    }

    public void setMediaController(MediaControllerCompat mediaController) {
        if (mediaController != null) {
            mControllerCallback = new ControllerCallback();
            mediaController.registerCallback(mControllerCallback);
            mControllerCallback.onMetadataChanged(mediaController.getMetadata());
            mControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
        } else if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
        }
        mMediaController = mediaController;
    }

    public void disconnectController() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
            mMediaController = null;
        }
    }

    private class ControllerCallback extends MediaControllerCompat.Callback implements ValueAnimator.AnimatorUpdateListener {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) return;
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            int progress = (int) state.getPosition();
            mProgressBar.setProgress(progress);
            mSeekBar.setProgress(progress);
            mProgressTextView.setText(makeTimeString((long) (progress) / 1000));

            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                int timeToEnd = (int) ((mDuration - progress) / state.getPlaybackSpeed());
                if (timeToEnd > 0) {
                    if (mProgressAnimator != null) mProgressAnimator.cancel();
                    mProgressAnimator = ValueAnimator.ofInt(progress, (int) mDuration).setDuration((long) (timeToEnd / mDurationScale));
                    mProgressAnimator.setInterpolator(new LinearInterpolator());
                    mProgressAnimator.addUpdateListener(this);
                    mProgressAnimator.start();
                }
            }
            if (state.getState() == PlaybackStateCompat.STATE_PAUSED || state.getState() == PlaybackStateCompat.STATE_NONE) {
                mBottomBarPlayPauseBtn.setImageDrawable(mBottomBarPlayPauseBtn.getContext().getDrawable(R.drawable.ic_play_arrow_black_24dp));
                mPlayPauseBtn.setImageDrawable(mPlayPauseBtn.getContext().getDrawable(R.drawable.ic_baseline_play_arrow_48));
            } else {
                mBottomBarPlayPauseBtn.setImageDrawable(mBottomBarPlayPauseBtn.getContext().getDrawable(R.drawable.ic_baseline_pause_24));
                mPlayPauseBtn.setImageDrawable(mPlayPauseBtn.getContext().getDrawable(R.drawable.ic_baseline_pause_48));
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata != null) {
                mDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                mProgressBar.setMax((int) mDuration);
                mSeekBar.setMax((int) mDuration);
            }
            if (mMediaController != null) {
                onPlaybackStateChanged(mMediaController.getPlaybackState());
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mSeekBarIsTracking) {
                animation.cancel();
                return;
            }
            int animatedValue = (int) animation.getAnimatedValue();
            mProgressBar.setProgress(animatedValue);
            mSeekBar.setProgress(animatedValue);
            mProgressTextView.setText(makeTimeString(animatedValue / 1000));
        }
    }
}
