package com.zionhuang.music.extensions

import androidx.paging.PagingSource.LoadResult
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.Page

fun <T : InfoItem> ListInfo<T>.toPage() = LoadResult.Page<Page, InfoItem>(
    data = relatedItems,
    nextKey = nextPage,
    prevKey = null
)

fun <T : InfoItem> InfoItemsPage<T>.toPage() = LoadResult.Page<Page, InfoItem>(
    data = items,
    nextKey = nextPage,
    prevKey = null
)