package com.zionhuang.music.playback;

import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import com.zionhuang.music.models.SongParcel;

import static com.zionhuang.music.playback.queue.Queue.QUEUE_NONE;

public class MediaSessionCallback extends MediaSessionCompat.Callback {
    private MediaSessionCompat mMediaSession;
    private SongPlayer mSongPlayer;

    MediaSessionCallback(MediaSessionCompat mediaSession, SongPlayer songPlayer) {
        mMediaSession = mediaSession;
        mSongPlayer = songPlayer;
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        SongParcel songParcel = extras.getParcelable("song");
        int queueType = extras.getInt("queueType");
        if (queueType == QUEUE_NONE) {
            throw new IllegalArgumentException("Unidentified queue type");
        }
        mSongPlayer.setQueue(queueType, mediaId);
        if (songParcel != null) {
            mSongPlayer.updateSongMeta(mediaId, songParcel);
        }
        mSongPlayer.playSong();
    }

    @Override
    public void onPlay() {
        mSongPlayer.play();
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

    @Override
    public void onSkipToNext() {
        mSongPlayer.playNext();
    }

    @Override
    public void onSkipToPrevious() {
        mSongPlayer.playPrevious();
    }
}
