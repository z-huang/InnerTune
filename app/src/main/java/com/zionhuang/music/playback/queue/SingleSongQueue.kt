package com.zionhuang.music.playback.queue

import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.db.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class SingleSongQueue(
        songsRepository: SongRepository,
        var songId: String
) : Queue {
    private var song: SongEntity = runBlocking(Dispatchers.IO) {
        songsRepository.getSongById(songId) ?: SongEntity(songId)
    }
    override var currentSongId: String?
        get() = songId
        set(_) {}
    override val currentSong: SongEntity
        get() = song
    override val previousSong: SongEntity? = currentSong
    override val nextSong: SongEntity? = currentSong

    override fun playNext() = Unit

    override fun playPrevious() = Unit

    override fun findSongById(id: String): SongEntity? = if (songId == id) song else null

    override fun updateSongMeta(id: String, songParcel: SongParcel) {
        if (songId == id) {
            song.apply {
                title = songParcel.title
                artist = songParcel.artist
            }
        }
    }
}