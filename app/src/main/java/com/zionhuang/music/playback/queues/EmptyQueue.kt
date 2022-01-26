package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

class EmptyQueue : Queue {
    override val items: List<MediaItem> = emptyList()
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()
}