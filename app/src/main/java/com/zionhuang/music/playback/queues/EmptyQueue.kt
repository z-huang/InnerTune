package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

class EmptyQueue : Queue {
    override suspend fun getInitialStatus() = Queue.Status(null, emptyList(), -1)
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()
}