package com.zionhuang.music.db

import android.content.Context
import com.zionhuang.music.db.daos.ArtistDao
import com.zionhuang.music.db.daos.ChannelDao
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.db.entities.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(context: Context) {
    companion object {
        private const val TAG = "SongRepository"
    }

    private val musicDatabase = MusicDatabase.getInstance(context)
    private val songDao: SongDao = musicDatabase.songDao
    private val artistDao: ArtistDao = musicDatabase.artistDao
    private val channelDao: ChannelDao = musicDatabase.channelDao

    suspend fun getAllXSongs() = withContext(Dispatchers.IO) {
        songDao.getAllXSongs()
    }

    fun getAllSongsAsFlow() = songDao.getAllSongsAsFlow()

    fun getAllSongsAsPagingSource() = songDao.getAllSongsAsPagingSource()

    fun getDownloadingSongsAsPagingSource() = songDao.getDownloadingSongsAsPagingSource()

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

    suspend fun deleteSongById(songId: String) = withContext(Dispatchers.IO) {
        songDao.deleteById(songId)
    }

    suspend fun hasSong(songId: String) = withContext(Dispatchers.IO) {
        songDao.contains(songId)
    }

    suspend fun getArtistIdByName(name: String) = withContext(Dispatchers.IO) {
        artistDao.getArtistIdByName(name)
    }

    suspend fun insertArtist(name: String) = withContext(Dispatchers.IO) {
        artistDao.insert(ArtistEntity(name = name))
    }

    suspend fun insertChannel(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        channelDao.insert(channel)
    }

    suspend fun hasChannel(channelId: String) = withContext(Dispatchers.IO) {
        channelDao.contains(channelId)
    }
}