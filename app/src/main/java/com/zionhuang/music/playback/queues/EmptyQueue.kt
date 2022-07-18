package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

class EmptyQueue : Queue {
    override val title: String? = null
    override val initialMediaItems: List<MediaItem> = emptyList()
    override val initialIndex: Int? = null
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()
}