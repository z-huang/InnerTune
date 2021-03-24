package com.zionhuang.music.youtube.newpipe

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.search.SearchInfo

object SearchCache {
    private val map = mutableMapOf<SearchQuery, SearchCacheData>()

    fun add(query: SearchQuery, cache: SearchCacheData) {
        if (query !in map) {
            map[query] = cache
        } else {
            map[query]!!.items.addAll(cache.items)
            map[query]!!.nextKey = cache.nextKey
        }
    }

    fun add(query: SearchQuery, searchInfo: SearchInfo) =
            add(query, SearchCacheData(searchInfo.relatedItems.toMutableList(), searchInfo.nextPage))

    fun add(query: SearchQuery, infoItemsPage: ListExtractor.InfoItemsPage<InfoItem>) =
            add(query, SearchCacheData(infoItemsPage.items.toMutableList(), infoItemsPage.nextPage))

    operator fun get(query: SearchQuery): SearchCacheData? = map[query]

    operator fun contains(query: SearchQuery) = query in map
}