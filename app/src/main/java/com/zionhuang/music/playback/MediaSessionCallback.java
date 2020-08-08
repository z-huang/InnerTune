package com.zionhuang.music.playback;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    private MediaSessionCompat mMediaSession;
    private SongPlayer mSongPlayer;

    MediaSessionCallback(MediaSessionCompat mediaSession, SongPlayer songPlayer) {
        mMediaSession = mediaSession;
        mSongPlayer = songPlayer;
    }

    @Override
    public void onPlay() {
        mSongPlayer.play();
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        mSongPlayer.playSong(mediaId, extras.getString("title"), extras.getString("artist"));
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        mSongPlayer.playSong(uri);
    }

    @Override
    public void onPause() {
        mSongPlayer.pause();
    }

    @Override
    public void onSeekTo(long pos) {
        mSongPlayer.seekTo(pos);
    }

    @Override
    public void onStop() {
        mSongPlayer.stop();
    }
}
