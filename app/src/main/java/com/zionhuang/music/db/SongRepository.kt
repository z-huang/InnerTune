package com.zionhuang.music.db

import android.content.Context
import com.zionhuang.music.constants.ORDER_ARTIST
import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.constants.SongSortType
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
    private val musicDatabase = MusicDatabase.getInstance(context)
    private val songDao: SongDao = musicDatabase.songDao
    private val artistDao: ArtistDao = musicDatabase.artistDao
    private val channelDao: ChannelDao = musicDatabase.channelDao

    fun getAllSongs(@SongSortType order: Int) = when (order) {
        ORDER_CREATE_DATE -> songDao.getAllSongsByCreateDateAsPagingSource()
        ORDER_NAME -> songDao.getAllSongsByName()
        ORDER_ARTIST -> songDao.getAllSongsByArtist()
        else -> throw IllegalArgumentException("Unexpected order type.")
    }

    val allSongsFlow get() = songDao.getAllSongsAsFlow()
    val allArtists get() = artistDao.getAllArtists()
    val allArtistsPagingSource get() = artistDao.getAllArtistsAsPagingSource()
    fun getArtistSongsAsPagingSource(artistId: Int) = songDao.getArtistSongsAsPagingSource(artistId)
    val allChannelsPagingSource get() = channelDao.getAllChannelsAsPagingSource()
    fun getChannelSongsAsPagingSource(channelId: String) = songDao.getChannelSongsAsPagingSource(channelId)

    suspend fun getSongById(id: String) = withContext(IO) { songDao.getSongById(id) }

    suspend fun insert(song: SongEntity) = withContext(IO) { songDao.insert(song) }
    suspend fun insert(song: Song) = withContext(IO) { songDao.insert(SongEntity(song.id, song.title, getOrInsertArtist(song.artistName), getOrInsertChannel(song.channelName), song.duration, song.liked, song.downloadState, song.createDate, song.modifyDate)) }

    suspend fun updateById(songId: String, applier: SongEntity.() -> Unit) = withContext(IO) {
        songDao.update(songDao.getSongEntityById(songId)!!.apply { applier(this) })
    }

    suspend fun updateSong(song: Song) = withContext(IO) {
        songDao.update(SongEntity(song.id, song.title, getOrInsertArtist(song.artistName), song.channelId, song.duration, song.liked, song.downloadState, song.createDate, song.modifyDate))
    }

    suspend fun deleteSongById(songId: String) = withContext(IO) {
        songDao.deleteById(songId)
        val songFile = File(context.getExternalFilesDir(null), "audio/$songId")
        if (songFile.exists()) {
            songFile.delete()
        }
    }

    suspend fun hasSong(songId: String) = withContext(IO) { songDao.contains(songId) }

    suspend fun getArtistById(artistId: Int) = withContext(IO) { artistDao.getArtistById(artistId) }

    suspend fun getArtistIdByName(name: String) = withContext(IO) { artistDao.getArtistIdByName(name) }

    fun findArtists(query: CharSequence) = artistDao.findArtists(query.toString())

    suspend fun insertArtist(name: String) = withContext(IO) { artistDao.insert(ArtistEntity(name = name)) }

    suspend fun getOrInsertArtist(name: String) = withContext(IO) {
        getArtistIdByName(name) ?: insertArtist(name).toInt()
    }

    suspend fun getChannel(channelId: String) = withContext(IO) { channelDao.getChannelById(channelId) }

    suspend fun getChannelByName(name: String) = withContext(IO) { channelDao.getChannelByName(name) }

    suspend fun insertChannel(channel: ChannelEntity) = withContext(IO) { channelDao.insert(channel) }

    suspend fun getOrInsertChannel(name: String) = withContext(IO) {
        getChannelByName(name) ?: insertChannel(ChannelEntity(name, name))
        return@withContext name
    }

    suspend fun hasChannel(channelId: String) = withContext(IO) { channelDao.contains(channelId) }

    suspend fun toggleLike(songId: String) {
        updateById(songId) {
            liked = !liked
        }
    }

    fun channelSongsCount(channelId: String) = songDao.channelSongsCount(channelId)

    fun channelSongsDuration(channelId: String) = songDao.channelSongsDuration(channelId)

    companion object {
        private const val TAG = "SongRepository"
    }
}