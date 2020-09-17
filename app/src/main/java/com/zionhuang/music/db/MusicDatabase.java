package com.zionhuang.music.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SongEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class MusicDatabase extends RoomDatabase {
    private static final String DBNAME = "song.db";
    private static volatile MusicDatabase mInstance;

    public static MusicDatabase getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MusicDatabase.class) {
                if (mInstance == null) {
                    mInstance = create(context);
                }
            }
        }
        return mInstance;
    }

    private static MusicDatabase create(Context context) {
        return Room.databaseBuilder(context, MusicDatabase.class, DBNAME).build();
    }

    public abstract SongDao getSongDao();
}
