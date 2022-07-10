package com.zionhuang.innertube

import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.YouTubeClient.Companion.ANDROID_MUSIC
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.response.*
import com.zionhuang.innertube.utils.insertSeparator
import io.ktor.client.call.*

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

    suspend fun getSearchSuggestions(query: String): List<BaseItem> =
        innerTube.getSearchSuggestions(ANDROID_MUSIC, query).body<GetSearchSuggestionsResponse>().contents
            .flatMap { section ->
                section.searchSuggestionsSectionRenderer.contents.mapNotNull { it.toItem() }
            }
            .insertSeparator { before, after ->
                if ((before is SuggestionTextItem && after !is SuggestionTextItem) || (before !is SuggestionTextItem && after is SuggestionTextItem)) Separator else null
            }

    suspend fun searchAllType(query: String): BrowseResult {
        val response = innerTube.search(WEB_REMIX, query).body<SearchResponse>()
//        val filters = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.header?.chipCloudRenderer?.chips
//            ?.filter { it.chipCloudChipRenderer.text != null }
//            ?.map { it.toFilter() }
        return BrowseResult(
            sections = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents
                .flatMap { it.toSections() }
                .map { if (it is Header) it.copy(moreNavigationEndpoint = null) else it },
            continuation = null
        )
    }

    suspend fun search(query: String, filter: SearchFilter): BrowseResult {
        val response = innerTube.search(WEB_REMIX, query, filter.value).body<SearchResponse>()
        return BrowseResult(
            sections = listOf(
                ListSection(
                    id = filter.value,
                    items = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.contents!!.map { it.toItem() },
                    continuation = response.contents.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.continuations?.getContinuation(),
                    itemViewType = Section.ViewType.LIST
                )
            ),
            continuation = response.contents.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.continuations?.getContinuation()
        )
    }

    suspend fun search(continuation: Continuation): BrowseResult {
        val response = innerTube.search(WEB_REMIX, continuation = continuation.value).body<SearchResponse>()
        return BrowseResult(
            sections = listOf(
                ListSection(
                    id = continuation.value,
                    items = response.continuationContents?.musicShelfContinuation?.contents?.map { it.toItem() }.orEmpty(),
                    continuation = response.continuationContents?.musicShelfContinuation?.continuations?.getContinuation(),
                    itemViewType = Section.ViewType.LIST
                )
            ),
            continuation = response.continuationContents?.musicShelfContinuation?.continuations?.getContinuation()
        )
    }

    suspend fun player(videoId: String, playlistId: String? = null): PlayerResponse =
        innerTube.player(ANDROID_MUSIC, videoId, playlistId).body()

    suspend fun browse(endpoint: BrowseEndpoint): BrowseResponse =
        innerTube.browse(WEB_REMIX, endpoint.browseId, endpoint.params, null).body()

    suspend fun browse(continuation: Continuation): BrowseResponse =
        innerTube.browse(WEB_REMIX, continuation = continuation.value).body()

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

    /**
     * Calling "next" endpoint, either ([index] == 0) or ([continuation] != null)
     */
    suspend fun getPlaylistItems(
        videoId: String,
        playlistId: String? = null,
        playlistSetVideoId: String? = null,
        index: Int? = null,
        params: String? = null,
        continuation: String? = null,
    ): NextResult {
        val response = innerTube.next(WEB_REMIX, videoId, playlistId, playlistSetVideoId, index, params, continuation).body<NextResponse>()
        return when {
            response.continuationContents != null -> NextResult(
                items = response.continuationContents.playlistPanelContinuation.contents
                    .map { it.playlistPanelVideoRenderer.toSongItem() },
                continuation = response.continuationContents.playlistPanelContinuation.continuations.getContinuation()
            )
            else -> NextResult(
                items = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0].tabRenderer.content!!.musicQueueRenderer?.content?.playlistPanelRenderer?.contents
                    ?.map { it.playlistPanelVideoRenderer.toSongItem() } ?: emptyList(),
                continuation = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0].tabRenderer.content!!.musicQueueRenderer?.content?.playlistPanelRenderer?.continuations?.getContinuation()
            )
        }
    }

    suspend fun getQueue(videoIds: List<String>? = null, playlistId: String? = null): List<SongItem> {
        if (videoIds != null) {
            assert(videoIds.size <= 1000) // Max video limit
        }
        return innerTube.getQueue(WEB_REMIX, videoIds, playlistId).body<GetQueueResponse>()
            .queueDatas.map {
                it.content.playlistPanelVideoRenderer.toSongItem()
            }
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

    @JvmInline
    value class Continuation(val value: String)

    const val HOME_BROWSE_ID = "FEmusic_home"
    const val EXPLORE_BROWSE_ID = "FEmusic_explore"
}