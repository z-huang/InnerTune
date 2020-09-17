package com.zionhuang.music.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SongParcel implements Parcelable {
    private String id;
    private String name;
    private String artist;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(artist);
    }

    public static final Parcelable.Creator<SongParcel> CREATOR = new Creator<SongParcel>() {
        @Override
        public SongParcel createFromParcel(Parcel source) {
            SongParcel song = new SongParcel();
            song.id = source.readString();
            song.artist = source.readString();
            song.artist = source.readString();
            return song;
        }

        @Override
        public SongParcel[] newArray(int size) {
            return new SongParcel[size];
        }
    };
}
