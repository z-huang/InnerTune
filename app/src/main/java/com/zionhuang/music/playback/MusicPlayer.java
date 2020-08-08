package com.zionhuang.music.playback;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;

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
        mPlayer.prepare(buildMediaSource(uri));
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ProgressiveMediaSource.Factory(mDataSourceFactory).createMediaSource(uri);
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
    public void onIsPlayingChanged(boolean isPlaying) {
        if (isPlaying) {
            if (mListener != null) {
                mListener.onPlaybackStateChanged(PlaybackStateCompat.STATE_PLAYING);
            }
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        @PlaybackStateCompat.State int state;
        switch (playbackState) {
            case Player.STATE_IDLE:
                state = PlaybackStateCompat.STATE_NONE;
                break;
            case Player.STATE_BUFFERING:
                state = PlaybackStateCompat.STATE_BUFFERING;
                break;
            case Player.STATE_READY:
                state = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
                break;
            case Player.STATE_ENDED:
                state = PlaybackStateCompat.STATE_NONE;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + playbackState);
        }
        if (mListener != null) {
            mListener.onPlaybackStateChanged(state);
        }
    }

    public void setListener(EventListener listener) {
        mListener = listener;
    }

    interface EventListener {
        void onPlaybackStateChanged(@PlaybackStateCompat.State int state);
    }
}
