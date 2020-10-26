package com.zionhuang.music.playback;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

/**
 * A wrapper around {@link SimpleExoPlayer}
 */

public class MusicPlayer implements SimpleExoPlayer.EventListener {
    private static final String TAG = "MusicPlayer";
    private SimpleExoPlayer mPlayer;
    private boolean mDurationSet = false;
    private EventListener mListener;

    MusicPlayer(Context context) {
        mPlayer = new SimpleExoPlayer.Builder(context).build();
        mPlayer.addListener(this);
        play();
    }

    public void play() {
        mPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    public void seekTo(long pos) {
        mPlayer.seekTo(pos);
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    public void stop() {
        mPlayer.stop(true);
    }

    public void setSource(Uri uri) {
        mPlayer.setMediaItem(MediaItem.fromUri(uri));
        mPlayer.prepare();
        mPlayer.seekTo(0);
        mDurationSet = false;
    }

    public long getPosition() {
        return mPlayer.getCurrentPosition();
    }

    public long getDuration() {
        return mPlayer.getDuration();
    }

    public void setPlaybackSpeed(float playbackSpeed) {
        mPlayer.setPlaybackParameters(new PlaybackParameters(playbackSpeed));
    }

    public float getPlaybackSpeed() {
        return mPlayer.getPlaybackParameters().speed;
    }

    public ExoPlayer getExoPlayer() {
        return mPlayer;
    }

    public void release() {
        mPlayer.release();
        mPlayer = null;
    }

    public void setPlayerView(PlayerView playerView) {
        if (playerView != null) {
            playerView.setPlayer(mPlayer);
        }
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        if (state == Player.STATE_READY) {
            if (!mDurationSet) {
                mDurationSet = true;
                mListener.onDurationSet(getDuration());
            }
        }
        if (mListener != null) {
            mListener.onPlaybackStateChanged(state);
        }
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        if (mPlayer.getPlaybackState() == Player.STATE_READY) {
            if (mListener != null) {
                mListener.onPlaybackStateChanged(Player.STATE_READY);
            }
        }
    }

    public void setListener(EventListener listener) {
        mListener = listener;
    }

    interface EventListener {
        void onPlaybackStateChanged(@Player.State int state);

        void onDurationSet(long duration);
    }
}
