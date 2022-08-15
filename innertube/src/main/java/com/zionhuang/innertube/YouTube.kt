package com.zionhuang.innertube

import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.YouTubeClient.Companion.ANDROID_MUSIC
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.response.*
import com.zionhuang.innertube.utils.insertSeparator
import io.ktor.client.call.*
import io.ktor.http.*

/**
 * Parse useful data with [InnerTube] sending requests.
 */
object YouTube {
    private val innerTube = InnerTube()

    var locale: YouTubeLocale
        get() = innerTube.locale
        set(value) {
            innerTube.locale = value
        }

    fun setProxyUrl(url: String) = innerTube.setProxyUrl(url)

    suspend fun getSearchSuggestions(query: String): List<YTBaseItem> =
        innerTube.getSearchSuggestions(ANDROID_MUSIC, query).body<GetSearchSuggestionsResponse>().contents
            .flatMap { section ->
                section.searchSuggestionsSectionRenderer.contents.mapNotNull { it.toItem() }
            }
            .insertSeparator { before, after ->
                if ((before is SuggestionTextItem && after !is SuggestionTextItem) || (before !is SuggestionTextItem && after is SuggestionTextItem)) Separator else null
            }

    suspend fun searchAllType(query: String): BrowseResult {
        val response = innerTube.search(WEB_REMIX, query).body<SearchResponse>()
        return BrowseResult(
            items = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents
                .flatMap { it.toBaseItems() }
                .map { if (it is Header) it.copy(moreNavigationEndpoint = null) else it },
            continuations = null
        )
    }

    suspend fun search(query: String, filter: SearchFilter): BrowseResult {
        val response = innerTube.search(WEB_REMIX, query, filter.value).body<SearchResponse>()
        return BrowseResult(
            items = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.contents!!.mapNotNull { it.toItem() },
            continuations = response.contents.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.continuations?.getContinuations()
        )
    }

    suspend fun search(continuation: String): BrowseResult {
        val response = innerTube.search(WEB_REMIX, continuation = continuation).body<SearchResponse>()
        return BrowseResult(
            items = response.continuationContents?.musicShelfContinuation?.contents?.mapNotNull { it.toItem() }.orEmpty(),
            continuations = response.continuationContents?.musicShelfContinuation?.continuations?.getContinuations()
        )
    }

    suspend fun player(videoId: String, playlistId: String? = null): PlayerResponse =
        innerTube.player(ANDROID_MUSIC, videoId, playlistId).body()

    suspend fun browse(endpoint: BrowseEndpoint): BrowseResult {
        val browseResult = innerTube.browse(WEB_REMIX, endpoint.browseId, endpoint.params, null).body<BrowseResponse>().toBrowseResult()
        if (endpoint.isAlbumEndpoint && browseResult.urlCanonical != null) {
            Url(browseResult.urlCanonical).parameters["list"]?.let { playlistId ->
                // replace video items with audio items
                return browseResult.copy(
                    items = browseResult.items.subList(0, browseResult.items.indexOfFirst { it is SongItem }) +
                            browse(BrowseEndpoint(browseId = "VL$playlistId")).items.filterIsInstance<SongItem>().mapIndexed { index, item ->
                                item.copy(
                                    subtitle = item.subtitle.split(" • ").lastOrNull().orEmpty(),
                                    index = (index + 1).toString()
                                )
                            } +
                            browseResult.items.subList(browseResult.items.indexOfLast { it is SongItem } + 1, browseResult.items.size)
                )
            }
        }
        return browseResult
    }

    suspend fun browse(continuation: String): BrowseResult =
        innerTube.browse(WEB_REMIX, continuation = continuation).body<BrowseResponse>().toBrowseResult()

    suspend fun browse(continuations: List<String>): BrowseResult {
        val result = browse(continuations[0])
        return result.copy(
            continuations = result.continuations.orEmpty() + continuations.drop(1)
        )
    }

    /**
     * Calling "next" endpoint without continuation
     * @return lyricsEndpoint, relatedEndpoint
     */
    suspend fun getPlaylistSongInfo(
        videoId: String,
        playlistId: String? = null,
        playlistSetVideoId: String? = null,
        index: Int? = null,
        params: String? = null,
    ): PlaylistSongInfo {
        val response = innerTube.next(WEB_REMIX, videoId, playlistId, playlistSetVideoId, index, params).body<NextResponse>()
        return PlaylistSongInfo(
            lyricsEndpoint = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[1].tabRenderer.endpoint!!.browseEndpoint!!,
            relatedEndpoint = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[2].tabRenderer.endpoint!!.browseEndpoint!!,
        )
    }

    suspend fun next(endpoint: WatchEndpoint, continuation: String? = null): NextResult {
        val response = innerTube.next(WEB_REMIX, endpoint.videoId, endpoint.playlistId, endpoint.playlistSetVideoId, endpoint.index, endpoint.params, continuation).body<NextResponse>()
        val playlistPanelRenderer = response.continuationContents?.playlistPanelContinuation ?: response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0].tabRenderer.content?.musicQueueRenderer?.content?.playlistPanelRenderer!!
        playlistPanelRenderer.contents.lastOrNull()?.automixPreviewVideoRenderer?.content?.automixPlaylistVideoRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.let { watchPlaylistEndpoint ->
            return next(watchPlaylistEndpoint.toWatchEndpoint()).let { result ->
                result.copy(
                    items = playlistPanelRenderer.contents.mapNotNull { it.playlistPanelVideoRenderer?.toSongItem() } + result.items,
                    currentIndex = playlistPanelRenderer.currentIndex
                )
            }
        }
        return NextResult(
            items = playlistPanelRenderer.contents.mapNotNull { it.playlistPanelVideoRenderer?.toSongItem() },
            currentIndex = playlistPanelRenderer.currentIndex,
            continuation = playlistPanelRenderer.continuations?.getContinuation()
        )
    }

    suspend fun getQueue(videoIds: List<String>? = null, playlistId: String? = null): List<SongItem> {
        if (videoIds != null) {
            assert(videoIds.size <= MAX_GET_QUEUE_SIZE) // Max video limit
        }
        return innerTube.getQueue(WEB_REMIX, videoIds, playlistId).body<GetQueueResponse>().queueDatas
            .mapNotNull { it.content.playlistPanelVideoRenderer?.toSongItem() }
    }

    @JvmInline
    value class SearchFilter(val value: String) {
        companion object {
            val FILTER_SONG = SearchFilter("EgWKAQIIAWoMEAMQDhAEEAkQChAF")
            val FILTER_VIDEO = SearchFilter("EgWKAQIQAWoMEAMQDhAEEAkQChAF")
            val FILTER_ALBUM = SearchFilter("EgWKAQIYAWoMEAMQDhAEEAkQChAF")
            val FILTER_ARTIST = SearchFilter("EgWKAQIgAWoMEAMQDhAEEAkQChAF")
            val FILTER_FEATURED_PLAYLIST = SearchFilter("EgeKAQQoADgBagwQAxAOEAQQCRAKEAU%3D")
            val FILTER_COMMUNITY_PLAYLIST = SearchFilter("EgeKAQQoAEABagwQAxAOEAQQCRAKEAU%3D")
        }
    }

    const val HOME_BROWSE_ID = "FEmusic_home"
    const val EXPLORE_BROWSE_ID = "FEmusic_explore"

    const val MAX_GET_QUEUE_SIZE = 1000
}