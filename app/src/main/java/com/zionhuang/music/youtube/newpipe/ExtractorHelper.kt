package com.zionhuang.music.youtube.newpipe

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.search.SearchInfo

@Suppress("BlockingMethodInNonBlockingContext")
object ExtractorHelper {
    suspend fun search(service: StreamingService, query: String, contentFilter: List<String>, sortFilter: String): SearchInfo = withContext(IO) {
        SearchInfo.getInfo(service, service.searchQHFactory.fromQuery(query, contentFilter, sortFilter))
    }

    suspend fun search(service: StreamingService, query: String, contentFilter: List<String>, sortFilter: String, page: Page): InfoItemsPage<InfoItem> = withContext(IO) {
        SearchInfo.getMoreItems(service, service.searchQHFactory.fromQuery(query, contentFilter, sortFilter), page)
    }
}