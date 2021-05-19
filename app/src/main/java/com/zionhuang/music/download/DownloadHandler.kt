package com.zionhuang.music.download

import com.zionhuang.music.ui.viewholders.SongViewHolder

class DownloadHandler {
    private val songs = mutableMapOf<String, SongViewHolder>()

    val downloadListener: DownloadListener = { task ->
        songs[task.id]?.setProgress(task)
    }

    fun add(songId: String, holder: SongViewHolder) {
        songs[songId] = holder
    }

    fun remove(songId: String) {
        songs.remove(songId)
    }
}