package com.zionhuang.music.ui.listeners

import android.content.Context
import com.zionhuang.music.db.entities.Song

interface SongPopupMenuListener {
    fun editSong(song: Song, context: Context)
    fun addToPlaylist(song: Song, context: Context)
    fun downloadSong(songId: String, context: Context)
    fun deleteSong(songId: String)
}