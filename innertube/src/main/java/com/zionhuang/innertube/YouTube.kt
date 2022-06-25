package com.zionhuang.innertube

import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.YouTubeClient.Companion.ANDROID_MUSIC
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.response.*
import io.ktor.client.call.*

/**
 * Parse useful data with [InnerTube] sending requests.
 */
class YouTube(locale: Locale) {
    private val innerTube = InnerTube(locale)

    suspend fun getSearchSuggestions(query: String): List<SuggestionItem> =
        innerTube.getSearchSuggestions(ANDROID_MUSIC, query).body<GetSearchSuggestionsResponse>()
            .contents.flatMap { section ->
                section.searchSuggestionsSectionRenderer.contents.map { it.toSuggestionItem() }
            }

    suspend fun searchAllType(query: String): SearchAllTypeResult {
        val response = innerTube.search(WEB_REMIX, query).body<SearchResponse>()
        return SearchAllTypeResult(
            filters = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.header?.chipCloudRenderer?.chips
                ?.filter { it.chipCloudChipRenderer.text != null }
                ?.map { it.toFilter() },
            sections = response.contents.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents
                .map { it.toItemSection() }
        )
    }

    suspend fun search(query: String, params: String): SearchResult {
        val response = innerTube.search(WEB_REMIX, query, params).body<SearchResponse>()
        return SearchResult(
            items = response.contents!!.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.contents
                .map { it.toItem() },
            continuation = response.contents.tabbedSearchResultsRenderer.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.continuations?.getContinuation()
        )
    }

    suspend fun search(continuation: String): SearchResult {
        val response = innerTube.search(WEB_REMIX, continuation = continuation).body<SearchResponse>()
        return SearchResult(
            items = response.continuationContents?.musicShelfContinuation?.contents?.map { it.toItem() }.orEmpty(),
            continuation = response.continuationContents?.musicShelfContinuation?.continuations?.getContinuation()
        )
    }

    suspend fun player(videoId: String, playlistId: String? = null): PlayerResponse =
        innerTube.player(ANDROID_MUSIC, videoId, playlistId).body()

    suspend fun browse(browseId: String, params: String? = null, continuation: String? = null): BrowseResponse =
        innerTube.browse(WEB_REMIX, browseId, params, continuation).body()


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
}