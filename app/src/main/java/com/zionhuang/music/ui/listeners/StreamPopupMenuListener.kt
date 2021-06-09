package com.zionhuang.music.ui.listeners

import android.content.Context
import org.schabi.newpipe.extractor.stream.StreamInfoItem

interface StreamPopupMenuListener {
    fun addToLibrary(songs: List<StreamInfoItem>)
    fun playNext(songs: List<StreamInfoItem>)
    fun addToQueue(songs: List<StreamInfoItem>)
    fun addToPlaylist(songs: List<StreamInfoItem>, context: Context)
    fun download(songs: List<StreamInfoItem>, context: Context)

    fun addToLibrary(song: StreamInfoItem) = addToLibrary(listOf(song))
    fun playNext(song: StreamInfoItem) = playNext(listOf(song))
    fun addToQueue(song: StreamInfoItem) = addToQueue(listOf(song))
    fun addToPlaylist(song: StreamInfoItem, context: Context) = addToPlaylist(listOf(song), context)
    fun download(song: StreamInfoItem, context: Context) = download(listOf(song), context)
}