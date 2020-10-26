package com.zionhuang.music.playback.queue

import com.zionhuang.music.models.SongParcel

class EmptyQueue private constructor() : Queue {
    companion object {
        @JvmField
        val EMPTY_QUEUE = EmptyQueue()
    }

    override var currentSongId: String?
        get() = null
        set(_) {}

    override val currentSong: Nothing? = null
    override val previousSong: Nothing? = null
    override val nextSong: Nothing? = null
    override fun playNext() = Unit
    override fun playPrevious() = Unit
    override fun findSongById(id: String): Nothing? = null
    override fun updateSongMeta(id: String, songParcel: SongParcel) = Unit
}