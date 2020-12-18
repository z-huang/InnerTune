package com.zionhuang.music.db

import android.content.Context
import com.zionhuang.music.db.daos.ArtistDao
import com.zionhuang.music.db.daos.ChannelDao
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

class SongRepository(private val context: Context) {
    companion object {
        private const val TAG = "SongRepository"
    }

    private val musicDatabase = MusicDatabase.getInstance(context)
    private val songDao: SongDao = musicDatabase.songDao
    private val artistDao: ArtistDao = musicDatabase.artistDao
    private val channelDao: ChannelDao = musicDatabase.channelDao

    val allSongsAsFlow get() = songDao.getAllSongsAsFlow()
    val allSongsAsPagingSource get() = songDao.getAllSongsAsPagingSource()
    val downloadingSongsPagingSource get() = songDao.getDownloadingSongsAsPagingSource()
    val allArtistsPagingSource get() = artistDao.getAllArtistsAsPagingSource()
    val allArtists get() = artistDao.getAllArtists()
    val allChannels get() = channelDao.getAllChannelsAsPagingSource()

    suspend fun getSongEntityById(id: String) = withContext(IO) { songDao.getSongEntityById(id) }

    suspend fun getSongById(id: String) = withContext(IO) { songDao.getSongById(id) }

    fun getSongAsFlow(songId: String) = songDao.getSongByIdAsFlowDistinct(songId)

    fun getArtistSongsAsPagingSource(artistId: Int) = songDao.getArtistSongsAsPagingSource(artistId)

    suspend fun insert(song: SongEntity) = withContext(IO) { songDao.insert(song) }
    suspend fun insert(song: Song) = withContext(IO) { songDao.insert(SongEntity(song.id, song.title, song.artistId, song.channelId, song.duration, song.liked, song.downloadState, song.createDate, song.modifyDate)) }

    suspend fun updateById(songId: String, applier: SongEntity.() -> Unit) = withContext(IO) {
        songDao.update(getSongEntityById(songId)!!.apply {
            applier(this)
        })
    }

    suspend fun updateSong(song: Song) = withContext(IO) {
        songDao.update(SongEntity(
                song.id,
                song.title,
                getOrInsertArtist(song.artistName),
                song.channelId,
                song.duration,
                song.liked,
                song.downloadState,
                song.createDate,
                song.modifyDate
        ))
    }

    suspend fun deleteSongById(songId: String) = withContext(IO) {
        songDao.deleteById(songId)
        val songFile = File(context.getExternalFilesDir(null), "audio/$songId")
        if (songFile.exists()) {
            songFile.delete()
        }
    }

    suspend fun hasSong(songId: String) = withContext(IO) { songDao.contains(songId) }

    suspend fun getArtistIdByName(name: String) = withContext(IO) { artistDao.getArtistIdByName(name) }

    fun findArtists(query: CharSequence) = artistDao.findArtists(query.toString())

    suspend fun insertArtist(name: String) = withContext(IO) { artistDao.insert(ArtistEntity(name = name)) }

    suspend fun getOrInsertArtist(name: String) = withContext(IO) {
        getArtistIdByName(name) ?: insertArtist(name).toInt()
    }

    suspend fun insertChannel(channel: ChannelEntity) = withContext(IO) { channelDao.insert(channel) }

    suspend fun hasChannel(channelId: String) = withContext(IO) { channelDao.contains(channelId) }

    suspend fun toggleLike(songId: String) {
        updateById(songId) {
            liked = !liked
        }
    }
}