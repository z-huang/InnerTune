package com.zionhuang.music.playback.queue

import android.os.Bundle
import com.zionhuang.music.constants.MediaConstants.SONG
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SongParcel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SingleSongQueue private constructor(
        song: Song,
) : Queue {
    override val currentSongId: String = song.id
    override val currentSong: Song = song
    override val previousSong: Song? = null
    override val nextSong: Song? = null

    override fun playNext(repeat: Boolean) = Unit
    override fun playPrevious() = Unit

    override fun findSongById(id: String): Song? = if (currentSong.id == id) currentSong else null

    override fun updateSongMeta(id: String, songParcel: SongParcel) {
        if (currentSong.id == id) {
            currentSong.apply {
                title = songParcel.title
                artistName = songParcel.artist.toString()
                channelId = songParcel.channelId
                channelName = songParcel.channelName
            }
        }
    }

    companion object {
        suspend fun create(songsRepository: SongRepository, scope: CoroutineScope, extras: Bundle): SingleSongQueue = withContext(Dispatchers.IO) {
            val songId = extras.getString("song_id")!!
            val song = songsRepository.getSongById(songId) ?: Song(songId)
            return@withContext SingleSongQueue(song).apply {
                extras.getParcelable<SongParcel>(SONG)?.let { songParcel ->
                    updateSongMeta(songId, songParcel)
                }
            }
        }
    }
}