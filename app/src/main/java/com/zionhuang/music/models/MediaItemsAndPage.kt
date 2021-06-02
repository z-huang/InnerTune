package com.zionhuang.music.models

import com.google.android.exoplayer2.MediaItem
import org.schabi.newpipe.extractor.Page

data class MediaItemsAndPage(
    val items: List<MediaItem>,
    val page: Page
)
