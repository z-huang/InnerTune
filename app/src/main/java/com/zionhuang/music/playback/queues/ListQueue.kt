package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem

class ListQueue(
    override val title: String?,
    items: List<MediaItem>,
    startIndex: Int,
) : Queue {
    override val initialMediaItems = items
    override val initialIndex = startIndex

    override fun hasNextPage(): Boolean = false

    override suspend fun nextPage() = throw UnsupportedOperationException()
}