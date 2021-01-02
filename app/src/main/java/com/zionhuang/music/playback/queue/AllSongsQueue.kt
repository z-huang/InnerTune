package com.zionhuang.music.playback.queue

import android.os.Bundle
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SongParcel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class AllSongsQueue private constructor(
        val list: MutableList<Song>,
        songId: String,
) : Queue {
    private var index: Int = list.indexOfFirst { it.id == songId }
    override val currentSongId: String? get() = list.getOrNull(index)?.id
    override val currentSong: Song? get() = list.getOrNull(index)
    override val previousSong: Song? get() = list.getOrNull(index - 1)
    override val nextSong: Song? get() = list.getOrNull(index + 1)
    override fun playNext(repeat: Boolean) {
        index++
        if (index == list.size && repeat) index = 0
    }

    override fun playPrevious() {
        index--
    }

    override fun findSongById(id: String): Song? = list.find { it.id == id }

    override fun updateSongMeta(id: String, songParcel: SongParcel) {
        list.find { it.id == id }?.apply {
            title = songParcel.title
            artistName = songParcel.artist.toString()
        }
    }

    companion object {
        const val TAG = "AllSongsQueue"
        suspend fun create(songsRepository: SongRepository, scope: CoroutineScope, extras: Bundle): AllSongsQueue = withContext(IO) {
            val list = songsRepository.getAllSongsMutableList(extras.getInt("sort_type"))
            return@withContext AllSongsQueue(list, extras.getString("song_id")!!)
        }
    }
}