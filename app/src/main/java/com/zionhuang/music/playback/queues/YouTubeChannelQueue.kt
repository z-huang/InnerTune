package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_CHANNEL
import com.zionhuang.music.extensions.toMediaItems
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.youtube.NewPipeYouTubeHelper
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.Page.isValid

class YouTubeChannelQueue(
    private val channelId: String,
    override val items: List<MediaItem>,
    page: Page?,
) : Queue {
    private var nextPage: Page? = page
    override fun hasNextPage() = isValid(nextPage)
    override suspend fun nextPage(): List<MediaItem> = if (hasNextPage()) {
        NewPipeYouTubeHelper.getChannel(channelId, nextPage!!).also { nextPage = it.nextPage }.items.toMediaItems()
    } else emptyList()

    companion object {
        const val TYPE = QUEUE_YT_CHANNEL
        val fromParcel: suspend (QueueData) -> Queue = { queueParcel ->
            val initialInfo = NewPipeYouTubeHelper.getChannel(queueParcel.queueId)
            YouTubeChannelQueue(
                queueParcel.queueId,
                initialInfo.relatedItems.toMediaItems(),
                initialInfo.nextPage
            )
        }
    }
}