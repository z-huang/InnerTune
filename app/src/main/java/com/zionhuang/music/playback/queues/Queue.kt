package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

interface Queue {
    val title: String?
    val initialMediaItems: List<MediaItem>
    val initialIndex: Int?
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<MediaItem>
}