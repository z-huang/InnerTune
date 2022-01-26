package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_SINGLE
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.youtube.NewPipeYouTubeHelper

class YouTubeSingleSongQueue(
    override val items: List<MediaItem>,
) : Queue {
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()

    companion object {
        const val TYPE = QUEUE_YT_SINGLE
        val fromParcel: suspend (QueueData) -> Queue = { queue ->
            YouTubeSingleSongQueue(listOf(NewPipeYouTubeHelper.getStreamInfo(queue.queueId).toMediaItem()))
        }
    }
}