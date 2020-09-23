package com.zionhuang.music.playback;

import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import com.zionhuang.music.models.SongParcel;

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
        SongParcel songParcel = extras.getParcelable("song");
        if (songParcel == null) {
            throw new IllegalArgumentException("Song Parcel can't be null.");
        }
        mSongPlayer.playSong(songParcel);
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
