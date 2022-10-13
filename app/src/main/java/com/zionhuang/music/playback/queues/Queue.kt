package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

interface Queue {
    suspend fun getInitialStatus(): Status
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<MediaItem>

    data class Status(
        val title: String?,
        val items: List<MediaItem>,
        val index: Int,
        val position: Long = 0L,
    )
}