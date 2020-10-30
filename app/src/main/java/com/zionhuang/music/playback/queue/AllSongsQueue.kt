package com.zionhuang.music.playback.queue

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AllSongsQueue(songsRepository: SongRepository, lifecycleOwner: LifecycleOwner) : Queue {
    companion object {
        const val TAG = "AllSongsQueue"
    }

    private val songsFlow = songsRepository.getAllSongsAsFlow()

    init {
        runBlocking {
            list = songsFlow.first()
        }
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            songsRepository.getAllSongsAsFlow().collect { l ->
                index = l.indexOfFirst { it.id == list[index].id }
                list = l
            }
        }
    }

    private var list: List<SongEntity>
    private var index: Int = -1
    override var currentSongId: String?
        get() = list.getOrNull(index)?.id
        set(songId) {
            index = list.indexOfFirst { it.id == songId }
        }
    override val currentSong: SongEntity?
        get() = list.getOrNull(index)
    override val previousSong: SongEntity?
        get() = list.getOrNull(if (index == 0) list.size - 1 else index - 1)
    override val nextSong: SongEntity?
        get() = list.getOrNull(if (index == list.size - 1) 0 else index + 1)

    override fun playNext() {
        index++
        if (index == list.size) index = 0
    }

    override fun playPrevious() {
        index--
        if (index == -1) index = list.size - 1
    }

    override fun findSongById(id: String): SongEntity? = list.find { it.id == id }

    override fun updateSongMeta(id: String, songParcel: SongParcel) {
        list.find { it.id == id }?.apply {
            title = songParcel.title
            artist = songParcel.artist
        }
    }

}