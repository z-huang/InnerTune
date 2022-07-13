package com.zionhuang.music.extensions

import androidx.paging.PagingSource.LoadResult
import com.zionhuang.innertube.models.BaseItem
import com.zionhuang.innertube.models.BrowseResult
import com.zionhuang.innertube.models.SearchAllTypeResult
import com.zionhuang.innertube.models.SearchResult
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

fun SearchAllTypeResult.toPage(): LoadResult.Page<String, BaseItem> = LoadResult.Page(
    data = items,
    nextKey = null,
    prevKey = null
)

fun SearchResult.toPage() = LoadResult.Page(
    data = items,
    nextKey = continuation,
    prevKey = null
)

fun BrowseResult.toPage() = LoadResult.Page(
    data = items,
    nextKey = continuation,
    prevKey = null
)