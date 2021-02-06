package com.zionhuang.music.ui.listeners

import android.content.Context
import android.view.View
import com.zionhuang.music.db.entities.Song

interface SongPopupMenuListener {
    fun editSong(song: Song, view: View)
    fun downloadSong(songId: String, context: Context)
    fun deleteSong(songId: String)
}