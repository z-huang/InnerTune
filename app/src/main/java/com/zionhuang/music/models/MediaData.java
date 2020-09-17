package com.zionhuang.music.models;

import android.graphics.Bitmap;
import android.support.v4.media.MediaMetadataCompat;

public class MediaData {
    private String mediaId = "";
    private String title = "";
    private String artist = "";
    private String album = "";
    private Bitmap artwork = null;
    private int duration = 0; // in seconds

    public MediaData() {
    }

    public MediaData pullMediaMetadata(MediaMetadataCompat mediaMetadata) {
        mediaId = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        album = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        artist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        duration = (int) (mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) / 1000);
        artwork = mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getDuration() {
        return duration;
    }
}
