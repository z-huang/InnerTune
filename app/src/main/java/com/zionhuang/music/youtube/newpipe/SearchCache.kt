package com.zionhuang.music.youtube.newpipe

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.search.SearchInfo

object SearchCache {
    private val map = mutableMapOf<String, SearchCacheData>()

    fun add(query: String, cache: SearchCacheData) {
        if (query !in map) {
            map[query] = cache
        } else {
            map[query]!!.items.addAll(cache.items)
            map[query]!!.nextKey = cache.nextKey
        }
    }

    fun add(query: String, searchInfo: SearchInfo) =
            add(query, SearchCacheData(searchInfo.relatedItems.toMutableList(), searchInfo.nextPage))

    fun add(query: String, infoItemsPage: ListExtractor.InfoItemsPage<InfoItem>) =
            add(query, SearchCacheData(infoItemsPage.items.toMutableList(), infoItemsPage.nextPage))

    operator fun get(query: String): SearchCacheData? = map[query]

    operator fun contains(query: String) = query in map
}