package com.zionhuang.music.playback.queue

import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AllSongsQueue(songsRepository: SongRepository, scope: CoroutineScope) : Queue {
    companion object {
        const val TAG = "AllSongsQueue"
    }

    private val songsFlow = songsRepository.allSongsFlow

    init {
        runBlocking {
            list = songsFlow.first()
        }
        scope.launch(IO) {
            songsRepository.allSongsFlow.collect { l ->
                index = l.indexOfFirst { it.id == list[index].id }
                list = l
            }
        }
    }

    private var list: List<Song>
    private var index: Int = -1
    override var currentSongId: String?
        get() = list.getOrNull(index)?.id
        set(songId) {
            index = list.indexOfFirst { it.id == songId }
        }
    override val currentSong: Song?
        get() = list.getOrNull(index)
    override val previousSong: Song?
        get() = list.getOrNull(if (index == 0) list.size - 1 else index - 1)
    override val nextSong: Song?
        get() = list.getOrNull(if (index == list.size - 1) 0 else index + 1)

    override fun playNext() {
        index++
        if (index == list.size) index = 0
    }

    override fun playPrevious() {
        index--
        if (index == -1) index = list.size - 1
    }

    override fun findSongById(id: String): Song? = list.find { it.id == id }

    override fun updateSongMeta(id: String, songParcel: SongParcel) {
        list.find { it.id == id }?.apply {
            title = songParcel.title
            artistName = songParcel.artist.toString()
        }
    }

}