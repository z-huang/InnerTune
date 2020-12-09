package com.zionhuang.music.db

import android.content.Context
import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(context: Context) {
    companion object {
        private const val TAG = "SongRepository"
    }

    private val songDao: SongDao = MusicDatabase.getInstance(context).songDao

    fun getAllSongsAsFlow(): Flow<List<SongEntity>> = songDao.getAllSongsAsFlow()

    fun getAllSongsAsPagingSource(): PagingSource<Int, SongEntity> = songDao.getAllSongsAsPagingSource()

    fun getDownloadingSongsAsPagingSource(): PagingSource<Int, SongEntity> = songDao.getDownloadingSongsAsPagingSource()

    suspend fun getSongById(id: String): SongEntity? = withContext(Dispatchers.IO) {
        songDao.getSongById(id)
    }

    fun getSongAsFlow(songId: String): Flow<SongEntity> = songDao.getSongByIdAsFlowDistinct(songId)

    suspend fun insert(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.insert(song)
    }

    suspend fun updateById(songId: String, applier: SongEntity.() -> Unit) = withContext(Dispatchers.IO) {
        songDao.update(getSongById(songId)!!.apply {
            applier(this)
        })
    }

    suspend fun deleteSong(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.delete(song)
    }
}