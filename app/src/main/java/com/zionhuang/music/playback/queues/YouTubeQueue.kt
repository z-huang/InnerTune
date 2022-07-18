package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.Item
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.VideoItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.music.extensions.toMediaItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeQueue(
    private val endpoint: WatchEndpoint,
    val item: Item? = null,
) : Queue {
    override val initialMediaItems: List<MediaItem> = when (item) {
        is SongItem -> listOf(item.toMediaItem())
        is VideoItem -> listOf(item.toMediaItem())
        else -> emptyList()
    }
    override val initialIndex: Int? = if (item is SongItem || item is VideoItem) 0 else null
    override val title: String? = null

    private var initialized = false
    private var continuation: String? = null


    override fun hasNextPage(): Boolean = !initialized || continuation != null

    override suspend fun nextPage(): List<MediaItem> {
        val nextResult = withContext(IO) { YouTube.next(endpoint, continuation) }
        continuation = nextResult.continuation
        initialized = true
        return nextResult.items.map { it.toMediaItem() }
    }
}