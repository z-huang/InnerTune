package com.zionhuang.music.playback.queues

import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.MediaConstants.EXTRA_SEARCH_FILTER
import com.zionhuang.music.constants.MediaConstants.QUEUE_YT_SEARCH
import com.zionhuang.music.extensions.toMediaItems
import com.zionhuang.music.models.QueueData
import com.zionhuang.music.youtube.NewPipeYouTubeHelper
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.Page.isValid

class YouTubeSearchQueue(
    private val query: String,
    private val filter: String,
    override val items: List<MediaItem>,
    page: Page?
) : Queue {
    private var nextPage: Page? = page
    override fun hasNextPage() = isValid(nextPage)
    override suspend fun nextPage(): List<MediaItem> = if (hasNextPage()) {
        NewPipeYouTubeHelper.search(query, listOf(filter), nextPage!!).also { nextPage = it.nextPage }.items.toMediaItems()
    } else emptyList()

    companion object {
        const val TYPE = QUEUE_YT_SEARCH
        val fromParcel: suspend (QueueData) -> Queue = {
            val initialInfo = NewPipeYouTubeHelper.search(it.queueId, listOf(it.extras.getString(EXTRA_SEARCH_FILTER)!!))
            YouTubeSearchQueue(
                it.queueId,
                it.extras.getString(EXTRA_SEARCH_FILTER)!!,
                initialInfo.relatedItems.toMediaItems(),
                initialInfo.nextPage
            )
        }
    }
}