package com.zionhuang.music.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.zionhuang.music.db.daos.RemoteDao;
import com.zionhuang.music.db.entities.RemoteKey;
import com.zionhuang.music.db.entities.SearchEntity;

@Database(entities = {RemoteKey.class, SearchEntity.class}, version = 1, exportSchema = false)
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
