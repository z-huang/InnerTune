package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

interface Queue {
    val title: String?
    suspend fun getInitialStatus(): Status
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<MediaItem>

    data class Status(
        val items: List<MediaItem>,
        val index: Int,
    )
}