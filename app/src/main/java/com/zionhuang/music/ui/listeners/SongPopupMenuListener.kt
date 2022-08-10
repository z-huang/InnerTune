package com.zionhuang.music.ui.listeners

import android.content.Context
import com.zionhuang.music.db.entities.Song

interface SongPopupMenuListener {
    fun editSong(song: Song, context: Context)
    fun playNext(songs: List<Song>, context: Context)
    fun addToQueue(songs: List<Song>, context: Context)
    fun addToPlaylist(songs: List<Song>, context: Context)
    fun downloadSongs(songs: List<Song>, context: Context)
    fun removeDownloads(songs: List<Song>, context: Context)
    fun deleteSongs(songs: List<Song>)

    fun playNext(song: Song, context: Context) = playNext(listOf(song), context)
    fun addToQueue(song: Song, context: Context) = addToQueue(listOf(song), context)
    fun addToPlaylist(song: Song, context: Context) = addToPlaylist(listOf(song), context)
    fun downloadSong(song: Song, context: Context) = downloadSongs(listOf(song), context)
    fun removeDownload(song: Song, context: Context) = removeDownloads(listOf(song), context)
    fun deleteSongs(song: Song) = deleteSongs(listOf(song))
}