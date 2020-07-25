package com.zionhuang.music.models;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaData {
    private String mediaId = "";
    private String title = "";
    private String artist = "";
    private String album = "";
    private Bitmap artwork = null;
    private long position = 0;
    private long duration = 0;
    private int shuffleMode = 0;
    private int repeatMode = 0;
    private int state = 0;

    public MediaData() {
    }

    public MediaData pullMediaMetadata(MediaMetadataCompat mediaMetadata) {
        mediaId = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        album = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        artist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        duration = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        artwork = mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        return this;
    }

    public MediaData pullPlaybackState(PlaybackStateCompat playbackState) {
        position = playbackState.getPosition();
        state = playbackState.getState();
        Bundle bundle = playbackState.getExtras();
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getDuration() {
        return duration;
    }
}
