package com.zionhuang.music.download

import com.zionhuang.music.ui.adapters.SongsAdapter

class DownloadHandler {
    private val songs = mutableMapOf<String, SongsAdapter.SongViewHolder>()
    val downloadListener: DownloadListener = { task ->
        songs[task.id]?.setProgress(task)
    }

    fun add(songId: String, holder: SongsAdapter.SongViewHolder) {
        songs[songId] = holder
    }

    fun remove(songId: String) {
        songs.remove(songId)
    }
}