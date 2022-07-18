package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

class EmptyQueue : Queue {
    override val title: String? = null
    override suspend fun getInitialStatus() = Queue.Status(emptyList(), -1)
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()
}