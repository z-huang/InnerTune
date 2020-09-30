package com.zionhuang.music.playback;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.zionhuang.music.R;

/**
 * A wrapper around {@link SimpleExoPlayer}
 */

public class MusicPlayer implements SimpleExoPlayer.EventListener {
    private static final String TAG = "MusicPlayer";
    private SimpleExoPlayer mPlayer;
    private DataSource.Factory mDataSourceFactory;
    private boolean mDurationSet = false;
    private EventListener mListener;

    MusicPlayer(Context context) {
        mPlayer = new SimpleExoPlayer.Builder(context).build();
        mPlayer.addListener(this);
        play();
        mDataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)));
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
        mPlayer.stop();
    }

    public void setSource(Uri uri) {
        mPlayer.setMediaSource(buildMediaSource(uri));
        mPlayer.prepare();
        mDurationSet = false;
    }

    private MediaSource buildMediaSource(Uri uri) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        return new ProgressiveMediaSource.Factory(mDataSourceFactory).createMediaSource(mediaItem);
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

    public void setListener(EventListener listener) {
        mListener = listener;
    }

    interface EventListener {
        void onPlaybackStateChanged(@Player.State int state);

        void onDurationSet(long duration);
    }
}
