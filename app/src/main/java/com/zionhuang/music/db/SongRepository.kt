package com.zionhuang.music.db

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(context: Context) {
    companion object {
        private const val TAG = "SongRepository"
    }

    private val songDao: SongDao = MusicDatabase.getInstance(context).songDao

    fun getAllSongsAsLiveData(): LiveData<List<SongEntity>> = songDao.getAllSongsAsLiveData()

    fun getAllSongsAsFlow(): Flow<List<SongEntity>> = songDao.getAllSongsAsFlow()

    fun getAllSongsAsPagingSource(): PagingSource<Int, SongEntity> = songDao.getAllSongsAsPagingSource()

    fun getDownloadingSongsAsPagingSource(): PagingSource<Int, SongEntity> = songDao.getDownloadingSongsAsPagingSource()

    suspend fun getSongById(id: String): SongEntity? = withContext(Dispatchers.IO) {
        songDao.getSongById(id)
    }

    fun getSongAsFlow(songId: String): Flow<SongEntity> = songDao.getSongByIdDistinctUntilChanged(songId)

    suspend fun insert(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.insert(song)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        songDao.deleteAll()
    }

    suspend fun updateById(songId: String, applier: SongEntity.() -> Unit) = withContext(Dispatchers.IO) {
        insert(getSongById(songId)!!.apply {
            applier(this)
        })
    }

    suspend fun hasSong(songId: String) = withContext(Dispatchers.IO) {
        songDao.contains(songId)
    }
}