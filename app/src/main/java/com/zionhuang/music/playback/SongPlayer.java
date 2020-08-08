package com.zionhuang.music.playback;

import android.content.Context;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.ui.PlayerView;
import com.zionhuang.music.R;
import com.zionhuang.music.extractor.YoutubeIE;
import com.zionhuang.music.utils.AsyncTask;

public class SongPlayer implements MusicPlayer.EventListener {
    private static final String TAG = "SongPlayer";
    private MusicPlayer mMusicPlayer;
    private MediaSessionCompat mMediaSession;

    SongPlayer(Context context) {
        mMusicPlayer = new MusicPlayer(context);
        mMusicPlayer.setListener(this);
        mMediaSession = new MediaSessionCompat(context, context.getString(R.string.app_name));
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setCallback(new MediaSessionCallback(mMediaSession, this));
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_NONE, 0, 1F).build());
        mMediaSession.setActive(true);
    }

    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }

    public void play() {
        mMusicPlayer.play();
    }

    public void playSong(String songId, String title, String artist) {
        updateMetadata(title, artist, 0);
        new AsyncTask<String, YoutubeIE.Result>()
                .doInBackground((id) -> new YoutubeIE().extract(id))
                .onDone((result) -> {
                    if (result.success) {
                        String url;
                        if (result.hasNormalStream()) {
                            url = result.getBestNormal().url;
                        } else {
                            url = result.getBestVideo().url;
                        }
                        mMusicPlayer.setSource(Uri.parse(url));
                    } else {
                        Log.d(TAG, "Extract failed, msg: " + result.errorMessage);
                    }
                }).execute(songId);
    }

    public void playSong(Uri uri) {
        mMusicPlayer.setSource(uri);
    }

    public void pause() {
        mMusicPlayer.pause();
        if (mMusicPlayer.isPlaying()) {
            mMusicPlayer.pause();
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, mMediaSession.getController().getPlaybackState().getPosition(), mMusicPlayer.getPlaybackSpeed());
        }
    }

    public void seekTo(long pos) {
        mMusicPlayer.seekTo(pos);
        updatePlaybackState(mMediaSession.getController().getPlaybackState().getState(), pos, mMusicPlayer.getPlaybackSpeed());
    }

    public void stop() {
        mMusicPlayer.stop();
        updatePlaybackState(PlaybackStateCompat.STATE_NONE, 0, mMusicPlayer.getPlaybackSpeed());
    }

    public void release() {
        mMediaSession.setActive(false);
        mMediaSession.release();
        mMusicPlayer.release();
    }

    public void setPlayerView(PlayerView playerView) {
        mMusicPlayer.setPlayerView(playerView);
    }

    private void updateMetadata(String title, String artist, long duration) {
        MediaMetadataCompat currentMetadata = mMediaSession.getController().getMetadata();
        setMetadata((currentMetadata == null ? new MediaMetadataCompat.Builder() : new MediaMetadataCompat.Builder(currentMetadata))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build());
    }

    private void setMetadata(MediaMetadataCompat metadata) {
        mMediaSession.setMetadata(metadata);
    }

    public void updatePlaybackState(@PlaybackStateCompat.State int state, long position, float playbackSpeed) {
        setPlaybackState(new PlaybackStateCompat.Builder(mMediaSession.getController().getPlaybackState())
                .setState(state, position, playbackSpeed)
                .build());
    }

    public void setPlaybackState(PlaybackStateCompat state) {
        mMediaSession.setPlaybackState(state);
    }

    @Override
    public void onPlaybackStateChanged(@PlaybackStateCompat.State int state) {
        updatePlaybackState(state, mMusicPlayer.getPosition(), mMusicPlayer.getPlaybackSpeed());
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            if (mMediaSession.getController().getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION) == 0) {
                // duration not set yet
                setMetadata(new MediaMetadataCompat.Builder(mMediaSession.getController().getMetadata())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mMusicPlayer.getDuration())
                        .build());
            }
        }
    }
}
