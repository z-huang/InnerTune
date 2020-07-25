package com.zionhuang.music.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.provider.Settings;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.zionhuang.music.R;

public class MediaSeekBar extends AppCompatSeekBar {
    private MediaControllerCompat mMediaController;
    private ControllerCallback mControllerCallback;
    private ValueAnimator mProgressAnimator;
    private float mDurationScale = Settings.Global.getFloat(getContext().getContentResolver(), Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
    private boolean mIsTracking = false;

    public MediaSeekBar(Context context) {
        this(context, null);
    }

    public MediaSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMediaController.getTransportControls().seekTo(getProgress());
                mIsTracking = false;
            }
        });
    }

    @Deprecated
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        throw new UnsupportedOperationException("Adding listener to MediaSeekBar denied");
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
            setProgress(progress);

            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());
                if (timeToEnd > 0) {
                    if (mProgressAnimator != null) mProgressAnimator.cancel();
                    mProgressAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration((long) (timeToEnd / mDurationScale));
                    mProgressAnimator.setInterpolator(new LinearInterpolator());
                    mProgressAnimator.addUpdateListener(this);
                    mProgressAnimator.start();
                }
            } else {
                setProgress((int) state.getPosition());
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata != null) {
                long max = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                setMax((int) max);
            }
            if (mMediaController != null) {
                onPlaybackStateChanged(mMediaController.getPlaybackState());
            }
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mIsTracking) {
                animation.cancel();
                return;
            }
            setProgress((int) animation.getAnimatedValue());
        }
    }
}
