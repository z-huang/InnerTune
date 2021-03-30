package com.zionhuang.music.youtube.newpipe

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.*
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.stream.StreamInfoItem

@Suppress("BlockingMethodInNonBlockingContext")
object ExtractorHelper {
    private val service = NewPipe.getService(ServiceList.YouTube.serviceId) as YoutubeService
    private val CACHE = InfoCache

    suspend fun search(query: String, contentFilter: List<String>): SearchInfo = withContext(IO) {
        SearchInfo.getInfo(service, service.searchQHFactory.fromQuery(query, contentFilter, ""))
    }

    suspend fun search(query: String, contentFilter: List<String>, page: Page): InfoItemsPage<InfoItem> = withContext(IO) {
        SearchInfo.getMoreItems(service, service.searchQHFactory.fromQuery(query, contentFilter, ""), page)
    }

    suspend fun getPlaylist(url: String): PlaylistInfo = checkCache(url, InfoType.PLAYLIST) {
        PlaylistInfo.getInfo(service, url)
    }

    suspend fun getPlaylist(url: String, nextPage: Page): InfoItemsPage<StreamInfoItem> = withContext(IO) {
        PlaylistInfo.getMoreItems(service, url, nextPage)
    }

    private suspend fun <T : Info> checkCache(url: String, infoType: InfoType, loadFromNetwork: suspend () -> T): T =
            loadFromCache(url, infoType) ?: withContext(IO) {
                loadFromNetwork().also {
                    InfoCache.putInfo(url, it, infoType)
                }
            }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Info> loadFromCache(url: String, infoType: InfoType): T? =
            InfoCache.getFromKey(url, infoType) as T?
}