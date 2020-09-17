package com.zionhuang.music;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.zionhuang.music.db.MusicDatabase;
import com.zionhuang.music.db.SongDao;
import com.zionhuang.music.db.SongEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    public final static SongEntity SONG = new SongEntity("_id");

    static {
        SONG.setTitle("_title");
        SONG.setArtist("_artist");
        SONG.setDuration(-1);
    }

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private SongDao songDao;
    private MusicDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, MusicDatabase.class)
                .allowMainThreadQueries()
                .build();
        songDao = db.getSongDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertionTest() {
        songDao.insert(SONG);
        songDao.getSongById(SONG.id)
                .test()
                .assertValue(s -> s.id.equals(SONG.id) &&
                        s.title.equals(SONG.title) &&
                        s.artist.equals(SONG.artist));
    }

    @Test
    public void updateTest() {
        songDao.insert(SONG);
        SONG.title = "_title2";
        songDao.update(SONG);
        songDao.getSongById(SONG.id)
                .test()
                .assertValue(s -> s.title.equals(SONG.title));
    }

    @Test
    public void deletionTest() {
        songDao.insert(SONG).blockingAwait();
        songDao.getSongById(SONG.id)
                .test()
                .assertValue(s -> s.duration == SONG.duration);
        songDao.delete(SONG);
        assertTrue(Objects.requireNonNull(songDao.getAllSongs().getValue()).isEmpty());
    }
}
