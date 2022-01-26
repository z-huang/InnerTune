package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

interface Queue {
    val items: List<MediaItem>
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<MediaItem>
}