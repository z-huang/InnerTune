package com.zionhuang.music.playback

import com.zionhuang.music.constants.MediaConstants.QUEUE_NONE
import com.zionhuang.music.constants.MediaConstants.QueueType
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.Page.isValid
import org.schabi.newpipe.extractor.linkhandler.LinkHandler

data class PlaylistData(
        @QueueType
        var queueType: Int = QUEUE_NONE,
        var nextPage: Page? = null,
        var linkHandler: LinkHandler? = null,
) {
    val hasMoreItems: Boolean get() = isValid(nextPage)
}