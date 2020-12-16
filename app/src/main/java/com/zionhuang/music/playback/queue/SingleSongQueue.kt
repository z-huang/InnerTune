package com.zionhuang.music.playback.queue

import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SongParcel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SingleSongQueue(
        songsRepository: SongRepository,
        var songId: String,
) : Queue {
    private var song: Song? = runBlocking(Dispatchers.IO) {
        songsRepository.getSongById(songId) ?: Song(songId)
    }
    override var currentSongId: String?
        get() = songId
        set(_) {}
    override val currentSong: Song?
        get() = song
    override val previousSong: Song? = currentSong
    override val nextSong: Song? = currentSong

    override fun playNext() = Unit

    override fun playPrevious() = Unit

    override fun findSongById(id: String): Song? = if (songId == id) song else null

    override fun updateSongMeta(id: String, songParcel: SongParcel) {
        if (songId == id) {
            song?.apply {
                title = songParcel.title
                artistName = songParcel.artist.toString()
            }
        }
    }
}