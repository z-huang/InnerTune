package com.zionhuang.music.ui.listeners

import android.content.Context
import com.zionhuang.music.db.entities.Song

interface SongPopupMenuListener {
    fun editSong(song: Song, context: Context)
    fun playNext(songs: List<Song>)
    fun addToQueue(songs: List<Song>)
    fun addToPlaylist(songs: List<Song>, context: Context)
    fun downloadSongs(songIds: List<String>, context: Context)
    fun deleteSongs(songs: List<Song>)
}