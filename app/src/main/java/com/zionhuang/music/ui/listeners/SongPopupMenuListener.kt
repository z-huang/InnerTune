package com.zionhuang.music.ui.listeners

import android.content.Context
import android.view.View

interface SongPopupMenuListener {
    fun editSong(songId: String, view: View)
    fun downloadSong(songId: String, context: Context)
    fun deleteSong(songId: String)
}