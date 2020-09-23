package com.zionhuang.music.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.zionhuang.music.db.SongEntity;

public class SongParcel implements Parcelable {
    private String id;
    private String title;
    private String artist;

    public SongParcel(String id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public static SongParcel fromSongEntity(SongEntity song) {
        return new SongParcel(song.id, song.title, song.artist);
    }

    public static SongParcel fromVideo(Video video) {
        return new SongParcel(video.getId(), video.getSnippet().getTitle(), video.getSnippet().getChannelTitle());
    }

    public static SongParcel fromSearchResult(SearchResult item) {
        if (!"youtube#video".equals(item.getId().getKind())) {
            throw new IllegalArgumentException("Can't convert a " + item.getId().getKind() + " item to SongParcel.");
        }
        return new SongParcel(item.getId().getVideoId(), item.getSnippet().getTitle(), item.getSnippet().getChannelTitle());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
    }

    public static final Parcelable.Creator<SongParcel> CREATOR = new Creator<SongParcel>() {
        @Override
        public SongParcel createFromParcel(Parcel source) {
            String id = source.readString();
            String name = source.readString();
            String artist = source.readString();
            return new SongParcel(id, name, artist);
        }

        @Override
        public SongParcel[] newArray(int size) {
            return new SongParcel[size];
        }
    };
}
