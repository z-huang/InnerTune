package com.zionhuang.music.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {RemoteKey.class, SearchEntity.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class RemoteDatabase extends RoomDatabase {
    private static final String DBNAME = "remote.db";
    private static volatile RemoteDatabase mInstance;

    public static RemoteDatabase getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RemoteDatabase.class) {
                if (mInstance == null) {
                    mInstance = create(context);
                }
            }
        }
        return mInstance;
    }

    private static RemoteDatabase create(Context context) {
        return Room.databaseBuilder(context, RemoteDatabase.class, DBNAME).build();
    }

    public abstract RemoteDao getRemoteDao();
}
