package com.zionhuang.music.playback.queue

import androidx.annotation.IntDef
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SongParcel

interface Queue {
    var currentSongId: String?
    val currentSong: Song?
    val previousSong: Song?
    val nextSong: Song?
    fun playNext()
    fun playPrevious()
    fun findSongById(id: String): Song?
    fun updateSongMeta(id: String, songParcel: SongParcel)

    companion object {
        @IntDef(QUEUE_NONE, QUEUE_ALL_SONG, QUEUE_PLAYLIST, QUEUE_ARTIST, QUEUE_SINGLE)
        @Retention(AnnotationRetention.SOURCE)
        annotation class QueueType

        const val QUEUE_NONE = 0
        const val QUEUE_ALL_SONG = 1
        const val QUEUE_PLAYLIST = 2
        const val QUEUE_ARTIST = 3
        const val QUEUE_SINGLE = 4
    }
}
