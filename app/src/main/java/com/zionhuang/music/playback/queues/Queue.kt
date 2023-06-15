package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.models.MediaMetadata

interface Queue {
    val preloadItem: MediaMetadata?
    suspend fun getInitialStatus(): Status
    fun hasNextPage(): Boolean
    suspend fun nextPage(): List<MediaItem>

    data class Status(
        val title: String?,
        val items: List<MediaItem>,
        val mediaItemIndex: Int,
        val position: Long = 0L,
    )
}
