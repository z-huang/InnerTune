package com.zionhuang.music.youtube.newpipe

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page

data class SearchCacheData(
        var items: MutableList<InfoItem>,
        var nextKey: Page?,
)
