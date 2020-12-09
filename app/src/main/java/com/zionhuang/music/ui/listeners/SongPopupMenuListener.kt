package com.zionhuang.music.ui.listeners

import android.content.Context
import android.view.View
import com.zionhuang.music.db.SongEntity

interface SongPopupMenuListener {
    fun editSong(songId: String, view: View)
    fun downloadSong(songId: String, context: Context)
    fun deleteSong(song: SongEntity)
}