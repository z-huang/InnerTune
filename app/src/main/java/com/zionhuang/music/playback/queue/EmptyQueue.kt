package com.zionhuang.music.playback.queue

import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SongParcel

class EmptyQueue private constructor() : Queue {
    companion object {
        @JvmField
        val EMPTY_QUEUE = EmptyQueue()
    }

    override var currentSongId: String? = null
    override val currentSong: Song? = null
    override val previousSong: Song? = null
    override val nextSong: Song? = null
    override fun playNext() = Unit
    override fun playPrevious() = Unit
    override fun findSongById(id: String): Song? = null
    override fun updateSongMeta(id: String, songParcel: SongParcel) = Unit
}