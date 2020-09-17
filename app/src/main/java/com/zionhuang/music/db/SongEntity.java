package com.zionhuang.music.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "song")
public class SongEntity {
    @NonNull
    @PrimaryKey
    public String id;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "artist")
    public String artist;
    @ColumnInfo(name = "duration")
    public int duration; // in seconds
    @ColumnInfo(name = "liked")
    public boolean liked;
    @ColumnInfo(name = "create_date")
    public Date createDate;
    @ColumnInfo(name = "modify_date")
    public Date modifyDate;

    public SongEntity(@NonNull String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
