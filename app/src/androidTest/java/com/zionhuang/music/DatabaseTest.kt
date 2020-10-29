package com.zionhuang.music

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.SongDao
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.extensions.getValueBlocking
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.*
import org.junit.Assert.assertNull
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @get:Rule
    var instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var db: MusicDatabase
    private lateinit var songDao: SongDao

    private val song = SongEntity(
            id = "test_id",
            title = "test_title",
            artist = "test_artist",
            duration = 10
    )

    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MusicDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        songDao = db.songDao
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    @Throws(IOException::class)
    fun closeDB() = db.close()

    @Test
    @Throws(Exception::class)
    fun insertionTest() {
        songDao.insert(song)
        songDao.getSongByIdAsSingle(song.id)
                .test()
                .assertValue {
                    it == song
                }
    }

    @Test
    fun updateTest() {
        songDao.insert(song)
        song.title = "test_title2"
        songDao.update(song)
        songDao.getSongByIdAsSingle(song.id)
                .test()
                .assertValue { (_, title) -> title == song.title }
    }

    @Test
    fun deletionTest() {
        songDao.insert(song)
        songDao.getSongByIdAsSingle(song.id)
                .test()
                .assertValue { (_, _, _, duration) -> duration == song.duration }
        songDao.delete(song)
        Assert.assertTrue(songDao.getAllSongsAsLiveData().getValueBlocking()!!.isEmpty())
    }

    @Test
    fun songNotFoundTest() {
        val song = songDao.getSongById("_")
        assertNull(song)
    }
}