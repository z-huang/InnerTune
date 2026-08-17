package com.zionhuang.innertube

import com.zionhuang.innertube.models.AccountInfo
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.Artist
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.GridRenderer
import com.zionhuang.innertube.models.MusicCarouselShelfRenderer
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SearchSuggestions
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_ATV
import com.zionhuang.innertube.models.YouTubeClient.Companion.ANDROID_MUSIC
import com.zionhuang.innertube.models.YouTubeClient.Companion.IOS
import com.zionhuang.innertube.models.YouTubeClient.Companion.TVHTML5
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB
import com.zionhuang.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.zionhuang.innertube.models.YouTubeLocale
import com.zionhuang.innertube.models.getContinuation
import com.zionhuang.innertube.models.oddElements
import com.zionhuang.innertube.models.response.AccountMenuResponse
import com.zionhuang.innertube.models.response.BrowseResponse
import com.zionhuang.innertube.models.response.GetQueueResponse
import com.zionhuang.innertube.models.response.GetSearchSuggestionsResponse
import com.zionhuang.innertube.models.response.GetTranscriptResponse
import com.zionhuang.innertube.models.response.NextResponse
import com.zionhuang.innertube.models.response.PipedResponse
import com.zionhuang.innertube.models.response.PlayerResponse
import com.zionhuang.innertube.models.response.SearchResponse
import com.zionhuang.innertube.pages.AlbumPage
import com.zionhuang.innertube.pages.ArtistItemsContinuationPage
import com.zionhuang.innertube.pages.ArtistItemsPage
import com.zionhuang.innertube.pages.ArtistPage
import com.zionhuang.innertube.pages.BrowseResult
import com.zionhuang.innertube.pages.ExplorePage
import com.zionhuang.innertube.pages.HomePage
import com.zionhuang.innertube.pages.MoodAndGenres
import com.zionhuang.innertube.pages.NewReleaseAlbumPage
import com.zionhuang.innertube.pages.NextPage
import com.zionhuang.innertube.pages.NextResult
import com.zionhuang.innertube.pages.PlaylistContinuationPage
import com.zionhuang.innertube.pages.PlaylistPage
import com.zionhuang.innertube.pages.RelatedPage
import com.zionhuang.innertube.pages.SearchPage
import com.zionhuang.innertube.pages.SearchResult
import com.zionhuang.innertube.pages.SearchSuggestionPage
import com.zionhuang.innertube.pages.SearchSummary
import com.zionhuang.innertube.pages.SearchSummaryPage
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.net.Proxy

// Piped API instances to try, in order, when resolving a fallback audio stream. Deliberately not
// every officially-listed instance (github.com/TeamPiped/documentation .../public-instances) --
// most of the full list, verified from GitHub Actions' real network on 2026-08-16, don't even
// resolve in DNS or fail TLS entirely (dead domains/certs, not transient outages -- retrying
// those adds guaranteed-failure latency with no realistic chance of success). pipedapi.kavin.rocks
// is kept first regardless of its state at verification time (HTTP 526 that day) since it's the
// official flagship instance; pipedapi.orangenet.cc was the only instance that returned a working
// response; pipedapi.adminforge.de responded (403, not a network/DNS/TLS failure) so is kept as a
// further fallback in case that was specific to one request/video rather than the whole instance
// being down.
internal val PIPED_INSTANCES = listOf(
    "https://pipedapi.kavin.rocks",
    "https://pipedapi.orangenet.cc",
    "https://pipedapi.adminforge.de",
)

/**
 * Pure retry/selection algorithm, independent of Ktor/networking so it's directly testable with a
 * fake [fetch]: tries [instances] in order (deduplicated, so an accidental repeat in the list is
 * never attempted twice), calling [fetch] for each. A thrown exception from [fetch] or an
 * empty/null result moves on to the next instance rather than stopping the whole lookup -- neither
 * is treated as fatal, since a different instance may still have a working answer. Returns the
 * first non-empty result; returns an empty list (never throws) once every instance has been tried
 * without one. [onResult] is called once per instance actually attempted (in order, before
 * short-circuiting) so a caller can log outcomes without affecting the selection itself.
 */
internal suspend fun <T> selectFirstNonEmpty(
    instances: List<String>,
    fetch: suspend (String) -> List<T>,
    onResult: (instance: String, result: Result<List<T>>) -> Unit = { _, _ -> },
): List<T> {
    for (instance in instances.distinct()) {
        val result = runCatching { fetch(instance) }
        onResult(instance, result)
        val value = result.getOrNull()
        if (!value.isNullOrEmpty()) {
            return value
        }
    }
    return emptyList()
}

// A Piped audio stream only reports itag/url/bitrate (see PipedResponse.AudioStream), not
// mimeType -- but downstream code (MusicService's FormatEntity storage, itag-based replay
// matching) needs one. These are YouTube's own itag -> container/codec assignments, stable and
// publicly documented for as long as these itags have existed; not a guess. Falls back to the
// current most common opus/webm mapping for any itag not listed here.
internal val PIPED_AUDIO_ITAG_MIME_TYPES: Map<Int, String> = mapOf(
    139 to "audio/mp4; codecs=\"mp4a.40.5\"",
    140 to "audio/mp4; codecs=\"mp4a.40.2\"",
    141 to "audio/mp4; codecs=\"mp4a.40.2\"",
    171 to "audio/webm; codecs=\"vorbis\"",
    172 to "audio/webm; codecs=\"vorbis\"",
    249 to "audio/webm; codecs=\"opus\"",
    250 to "audio/webm; codecs=\"opus\"",
    251 to "audio/webm; codecs=\"opus\"",
)

// Piped doesn't report an expiry for its stream URLs. This is a conservative, fixed assumption
// (matching the ~6-hour expiry YouTube's own streamingData.expiresInSeconds typically reports)
// used only for MusicService's local songUrlCache housekeeping -- an underestimate just causes
// an extra, harmless re-resolve; it doesn't affect whether the stream itself is playable.
internal const val PIPED_STREAM_ASSUMED_EXPIRY_SECONDS = 21_600

/**
 * Packages a Piped audio stream as a [PlayerResponse.StreamingData.Format] so it can flow through
 * the same MusicService code path as a normal YouTube-sourced format. Fields YouTube's response
 * would normally carry but Piped doesn't report (dimensions, duration, sample rate, etc.) are left
 * null; only [PlayerResponse.StreamingData.Format.itag]/[url]/[mimeType]/[bitrate] are ever read
 * by MusicService.
 */
internal fun PipedResponse.AudioStream.toFormat() = PlayerResponse.StreamingData.Format(
    itag = itag,
    url = url,
    mimeType = PIPED_AUDIO_ITAG_MIME_TYPES[itag] ?: "audio/webm; codecs=\"opus\"",
    bitrate = bitrate,
    width = null,
    height = null,
    contentLength = null,
    quality = "medium",
    fps = null,
    qualityLabel = null,
    averageBitrate = bitrate,
    audioQuality = null,
    approxDurationMs = null,
    audioSampleRate = null,
    audioChannels = null,
    loudnessDb = null,
    lastModified = null,
)

// A client can report playabilityStatus.status == "OK" while every one of its adaptiveFormats
// still has a null url (observed on a real device for IOS immediately after the clientId header
// fix: status=OK, 23 formats, but the selected format's url was null). This most likely means
// YouTube omitted the direct url for that client/request (e.g. a signatureCipher-only format our
// [PlayerResponse.StreamingData.Format] model doesn't parse, or a PoToken-gated format) rather
// than the format being genuinely unusable -- either way, "OK" alone is not sufficient evidence
// that this response is actually playable. Used to decide whether a client's "OK" response should
// short-circuit the ANDROID_MUSIC -> IOS -> TVHTML5/Piped fallback chain below. Public (not
// internal) so the app module can reuse the same check for its WEB_REMIX+PoToken attempt.
fun PlayerResponse.hasPlayableAudioFormat(): Boolean =
    streamingData?.adaptiveFormats?.any { it.isAudio && it.url != null } == true

/**
 * Parse useful data with [InnerTube] sending requests.
 * Modified from [ViMusic](https://github.com/vfsfitvnm/ViMusic)
 */
object YouTube {
    private val innerTube = InnerTube()

    var locale: YouTubeLocale
        get() = innerTube.locale
        set(value) {
            innerTube.locale = value
        }
    var visitorData: String
        get() = innerTube.visitorData
        set(value) {
            innerTube.visitorData = value
        }
    var cookie: String?
        get() = innerTube.cookie
        set(value) {
            innerTube.cookie = value
        }
    var proxy: Proxy?
        get() = innerTube.proxy
        set(value) {
            innerTube.proxy = value
        }
    var useLoginForBrowse: Boolean
        get() = innerTube.useLoginForBrowse
        set(value) {
            innerTube.useLoginForBrowse = value
        }

    suspend fun searchSuggestions(query: String): Result<SearchSuggestions> = runCatching {
        val response = innerTube.getSearchSuggestions(WEB_REMIX, query).body<GetSearchSuggestionsResponse>()
        SearchSuggestions(
            queries = response.contents?.getOrNull(0)?.searchSuggestionsSectionRenderer?.contents?.mapNotNull { content ->
                content.searchSuggestionRenderer?.suggestion?.runs?.joinToString(separator = "") { it.text }
            }.orEmpty(),
            recommendedItems = response.contents?.getOrNull(1)?.searchSuggestionsSectionRenderer?.contents?.mapNotNull {
                it.musicResponsiveListItemRenderer?.let { renderer ->
                    SearchSuggestionPage.fromMusicResponsiveListItemRenderer(renderer)
                }
            }.orEmpty()
        )
    }

    suspend fun searchSummary(query: String): Result<SearchSummaryPage> = runCatching {
        val response = innerTube.search(WEB_REMIX, query).body<SearchResponse>()
        SearchSummaryPage(
            summaries = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.mapNotNull { it ->
                if (it.musicCardShelfRenderer != null)
                    SearchSummary(
                        title = it.musicCardShelfRenderer.header.musicCardShelfHeaderBasicRenderer.title.runs?.firstOrNull()?.text ?: return@mapNotNull null,
                        items = listOfNotNull(SearchSummaryPage.fromMusicCardShelfRenderer(it.musicCardShelfRenderer))
                            .plus(
                                it.musicCardShelfRenderer.contents
                                    ?.mapNotNull { it.musicResponsiveListItemRenderer }
                                    ?.mapNotNull(SearchSummaryPage.Companion::fromMusicResponsiveListItemRenderer)
                                    .orEmpty()
                            )
                            .distinctBy { it.id }
                            .ifEmpty { null } ?: return@mapNotNull null
                    )
                else
                    SearchSummary(
                        title = it.musicShelfRenderer?.title?.runs?.firstOrNull()?.text ?: return@mapNotNull null,
                        items = it.musicShelfRenderer.contents
                            ?.mapNotNull {
                                SearchSummaryPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                            }
                            ?.distinctBy { it.id }
                            ?.ifEmpty { null } ?: return@mapNotNull null
                    )
            }!!
        )
    }

    suspend fun search(query: String, filter: SearchFilter): Result<SearchResult> = runCatching {
        val response = innerTube.search(WEB_REMIX, query, filter.value).body<SearchResponse>()
        SearchResult(
            items = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.lastOrNull()
                ?.musicShelfRenderer?.contents?.mapNotNull {
                    SearchPage.toYTItem(it.musicResponsiveListItemRenderer)
                }.orEmpty(),
            continuation = response.contents?.tabbedSearchResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents?.lastOrNull()
                ?.musicShelfRenderer?.continuations?.getContinuation()
        )
    }

    suspend fun searchContinuation(continuation: String): Result<SearchResult> = runCatching {
        val response = innerTube.search(WEB_REMIX, continuation = continuation).body<SearchResponse>()
        SearchResult(
            items = response.continuationContents?.musicShelfContinuation?.contents
                ?.mapNotNull {
                    SearchPage.toYTItem(it.musicResponsiveListItemRenderer)
                }!!,
            continuation = response.continuationContents.musicShelfContinuation.continuations?.getContinuation()
        )
    }

    suspend fun album(browseId: String, withSongs: Boolean = true): Result<AlbumPage> = runCatching {
        val response = innerTube.browse(WEB_REMIX, browseId).body<BrowseResponse>()
        val playlistId = response.microformat?.microformatDataRenderer?.urlCanonical?.substringAfterLast('=')!!
        AlbumPage(
            album = AlbumItem(
                browseId = browseId,
                playlistId = playlistId,
                title = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.title?.runs?.firstOrNull()?.text!!,
                artists = response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.straplineTextOne?.runs?.oddElements()?.map {
                    Artist(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId
                    )
                }!!,
                year = response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.subtitle?.runs?.lastOrNull()?.text?.toIntOrNull(),
                thumbnail = response.contents.twoColumnBrowseResultsRenderer.tabs.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.musicResponsiveHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url!!,
            ),
            songs = if (withSongs) albumSongs(playlistId).getOrThrow() else emptyList(),
            otherVersions = response.contents.twoColumnBrowseResultsRenderer.secondaryContents?.sectionListRenderer?.contents?.getOrNull(1)?.musicCarouselShelfRenderer?.contents
                ?.mapNotNull { it.musicTwoRowItemRenderer }
                ?.mapNotNull(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer)
                .orEmpty()
        )
    }

    suspend fun albumSongs(playlistId: String): Result<List<SongItem>> = runCatching {
        var response = innerTube.browse(WEB_REMIX, "VL$playlistId").body<BrowseResponse>()
        val songs = response.contents?.twoColumnBrowseResultsRenderer
            ?.secondaryContents?.sectionListRenderer
            ?.contents?.firstOrNull()
            ?.musicPlaylistShelfRenderer?.contents
            ?.mapNotNull {
                AlbumPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
            }!!
            .toMutableList()
        var continuation = response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer
            ?.contents?.firstOrNull()?.musicPlaylistShelfRenderer?.continuations?.getContinuation()
        while (continuation != null) {
            response = innerTube.browse(
                client = WEB_REMIX,
                continuation = continuation,
            ).body<BrowseResponse>()
            songs += response.continuationContents?.musicPlaylistShelfContinuation?.contents?.mapNotNull {
                AlbumPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
            }.orEmpty()
            continuation = response.continuationContents?.musicPlaylistShelfContinuation?.continuations?.getContinuation()
        }
        songs
    }

    suspend fun artist(browseId: String): Result<ArtistPage> = runCatching {
        val response = innerTube.browse(WEB_REMIX, browseId).body<BrowseResponse>()
        ArtistPage(
            artist = ArtistItem(
                id = browseId,
                title = response.header?.musicImmersiveHeaderRenderer?.title?.runs?.firstOrNull()?.text
                    ?: response.header?.musicVisualHeaderRenderer?.title?.runs?.firstOrNull()?.text!!,
                thumbnail = response.header?.musicImmersiveHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                    ?: response.header?.musicVisualHeaderRenderer?.foregroundThumbnail?.musicThumbnailRenderer?.getThumbnailUrl()!!,
                shuffleEndpoint = response.header?.musicImmersiveHeaderRenderer?.playButton?.buttonRenderer?.navigationEndpoint?.watchEndpoint,
                radioEndpoint = response.header?.musicImmersiveHeaderRenderer?.startRadioButton?.buttonRenderer?.navigationEndpoint?.watchEndpoint
            ),
            sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                ?.tabRenderer?.content?.sectionListRenderer?.contents
                ?.mapNotNull(ArtistPage::fromSectionListRendererContent)!!,
            description = response.header?.musicImmersiveHeaderRenderer?.description?.runs?.firstOrNull()?.text
        )
    }

    suspend fun artistItems(endpoint: BrowseEndpoint): Result<ArtistItemsPage> = runCatching {
        val response = innerTube.browse(WEB_REMIX, endpoint.browseId, endpoint.params).body<BrowseResponse>()
        val gridRenderer = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
            ?.gridRenderer
        if (gridRenderer != null) {
            ArtistItemsPage(
                title = gridRenderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text.orEmpty(),
                items = gridRenderer.items.mapNotNull {
                    it.musicTwoRowItemRenderer?.let { renderer ->
                        ArtistItemsPage.fromMusicTwoRowItemRenderer(renderer)
                    }
                },
                continuation = gridRenderer.continuations?.getContinuation()
            )
        } else {
            ArtistItemsPage(
                title = response.header?.musicHeaderRenderer?.title?.runs?.firstOrNull()?.text!!,
                items = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                    ?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                        ArtistItemsPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                    }!!,
                continuation = response.contents.singleColumnBrowseResultsRenderer.tabs.firstOrNull()
                    ?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
                    ?.musicPlaylistShelfRenderer?.continuations?.getContinuation()
            )
        }
    }

    suspend fun artistItemsContinuation(continuation: String): Result<ArtistItemsContinuationPage> = runCatching {
        val response = innerTube.browse(WEB_REMIX, continuation = continuation).body<BrowseResponse>()
        val gridContinuation = response.continuationContents?.gridContinuation
        if (gridContinuation != null) {
            ArtistItemsContinuationPage(
                items = gridContinuation.items.mapNotNull {
                    it.musicTwoRowItemRenderer?.let { renderer ->
                        ArtistItemsPage.fromMusicTwoRowItemRenderer(renderer)
                    }
                },
                continuation = gridContinuation.continuations?.getContinuation()
            )
        } else {
            ArtistItemsContinuationPage(
                items = response.continuationContents?.musicPlaylistShelfContinuation?.contents?.mapNotNull {
                    ArtistItemsPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                }!!,
                continuation = response.continuationContents.musicPlaylistShelfContinuation.continuations?.getContinuation()
            )
        }
    }

    suspend fun playlist(playlistId: String): Result<PlaylistPage> = runCatching {
        val response = innerTube.browse(
            client = WEB_REMIX,
            browseId = "VL$playlistId",
            setLogin = true
        ).body<BrowseResponse>()
        val base = response.contents?.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()
        val header = base?.musicResponsiveHeaderRenderer ?: base?.musicEditablePlaylistDetailHeaderRenderer?.header?.musicResponsiveHeaderRenderer
        PlaylistPage(
            playlist = PlaylistItem(
                id = playlistId,
                title = header?.title?.runs?.firstOrNull()?.text!!,
                author = header.straplineTextOne?.runs?.firstOrNull()?.let {
                    Artist(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId
                    )
                },
                songCountText = header.secondSubtitle?.runs?.firstOrNull()?.text,
                thumbnail = header.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.lastOrNull()?.url!!,
                playEndpoint = null,
                shuffleEndpoint = header.buttons?.lastOrNull()?.menuRenderer?.items?.firstOrNull()?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint!!,
                radioEndpoint = header.buttons.lastOrNull()?.menuRenderer?.items!!.find {
                    it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
                }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint!!
            ),
            songs = response.contents?.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.contents
                ?.firstOrNull()?.musicPlaylistShelfRenderer?.contents?.mapNotNull {
                    PlaylistPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
                }!!,
            songsContinuation = response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
                .contents.firstOrNull()
                ?.musicPlaylistShelfRenderer?.continuations?.getContinuation(),
            continuation = response.contents.twoColumnBrowseResultsRenderer.secondaryContents.sectionListRenderer
                .continuations?.getContinuation()
        )
    }

    suspend fun playlistContinuation(continuation: String) = runCatching {
        val response = innerTube.browse(
            client = WEB_REMIX,
            continuation = continuation,
            setLogin = true
        ).body<BrowseResponse>()
        PlaylistContinuationPage(
            songs = response.continuationContents?.musicPlaylistShelfContinuation?.contents?.mapNotNull {
                PlaylistPage.fromMusicResponsiveListItemRenderer(it.musicResponsiveListItemRenderer)
            }!!,
            continuation = response.continuationContents.musicPlaylistShelfContinuation.continuations?.getContinuation()
        )
    }

    suspend fun home(): Result<HomePage> = runCatching {
        var response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_home").body<BrowseResponse>()
        var continuation = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.continuations?.getContinuation()
        val sections = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()
            ?.tabRenderer?.content?.sectionListRenderer?.contents!!
            .mapNotNull { it.musicCarouselShelfRenderer }
            .mapNotNull {
                HomePage.Section.fromMusicCarouselShelfRenderer(it)
            }.toMutableList()
        while (continuation != null) {
            response = innerTube.browse(WEB_REMIX, continuation = continuation).body<BrowseResponse>()
            continuation = response.continuationContents?.sectionListContinuation?.continuations?.getContinuation()
            sections += response.continuationContents?.sectionListContinuation?.contents
                ?.mapNotNull { it.musicCarouselShelfRenderer }
                ?.mapNotNull {
                    HomePage.Section.fromMusicCarouselShelfRenderer(it)
                }.orEmpty()
        }
        HomePage(sections)
    }

    suspend fun explore(): Result<ExplorePage> = runCatching {
        val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_explore").body<BrowseResponse>()
        ExplorePage(
            newReleaseAlbums = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.find {
                it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId == "FEmusic_new_releases_albums"
            }?.musicCarouselShelfRenderer?.contents
                ?.mapNotNull { it.musicTwoRowItemRenderer }
                ?.mapNotNull(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer).orEmpty(),
            moodAndGenres = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.find {
                it.musicCarouselShelfRenderer?.header?.musicCarouselShelfBasicHeaderRenderer?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint?.browseId == "FEmusic_moods_and_genres"
            }?.musicCarouselShelfRenderer?.contents
                ?.mapNotNull { it.musicNavigationButtonRenderer }
                ?.mapNotNull(MoodAndGenres.Companion::fromMusicNavigationButtonRenderer)
                .orEmpty()
        )
    }

    suspend fun newReleaseAlbums(): Result<List<AlbumItem>> = runCatching {
        val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_new_releases_albums").body<BrowseResponse>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.gridRenderer?.items
            ?.mapNotNull { it.musicTwoRowItemRenderer }
            ?.mapNotNull(NewReleaseAlbumPage::fromMusicTwoRowItemRenderer)
            .orEmpty()
    }

    suspend fun moodAndGenres(): Result<List<MoodAndGenres>> = runCatching {
        val response = innerTube.browse(WEB_REMIX, browseId = "FEmusic_moods_and_genres").body<BrowseResponse>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents!!
            .mapNotNull(MoodAndGenres.Companion::fromSectionListRendererContent)
    }

    suspend fun browse(browseId: String, params: String?): Result<BrowseResult> = runCatching {
        val response = innerTube.browse(WEB_REMIX, browseId = browseId, params = params).body<BrowseResponse>()
        BrowseResult(
            title = response.header?.musicHeaderRenderer?.title?.runs?.firstOrNull()?.text,
            items = response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.mapNotNull { content ->
                when {
                    content.gridRenderer != null -> {
                        BrowseResult.Item(
                            title = content.gridRenderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text,
                            items = content.gridRenderer.items
                                .mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
                                .mapNotNull(RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                        )
                    }

                    content.musicCarouselShelfRenderer != null -> {
                        BrowseResult.Item(
                            title = content.musicCarouselShelfRenderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text,
                            items = content.musicCarouselShelfRenderer.contents
                                .mapNotNull(MusicCarouselShelfRenderer.Content::musicTwoRowItemRenderer)
                                .mapNotNull(RelatedPage.Companion::fromMusicTwoRowItemRenderer)
                        )
                    }

                    else -> null
                }
            }.orEmpty()
        )
    }

    suspend fun likedPlaylists(): Result<List<PlaylistItem>> = runCatching {
        val response = innerTube.browse(
            client = WEB_REMIX,
            browseId = "FEmusic_liked_playlists",
            setLogin = true
        ).body<BrowseResponse>()
        response.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()?.gridRenderer?.items!!
            .drop(1) // the first item is "create new playlist"
            .mapNotNull(GridRenderer.Item::musicTwoRowItemRenderer)
            .mapNotNull {
                ArtistItemsPage.fromMusicTwoRowItemRenderer(it) as? PlaylistItem
            }
    }

    // Diagnostic-only: which client(s) player() tried, whether each returned a playable status,
    // and how many adaptive formats it offered. Never logs a URL, cookie, or auth header -- only
    // client name, video id, playability status/reason, and a format count. System.err is used
    // (rather than an Android logger) because this module is a plain Kotlin/JVM library with no
    // Android dependency; System.err/out from the app process is still captured by logcat.
    private fun logPlayerAttempt(client: String, videoId: String, response: PlayerResponse) {
        System.err.println(
            "[InnerTube.player] client=$client videoId=$videoId status=${response.playabilityStatus.status} " +
                "reason=${response.playabilityStatus.reason} formats=${response.streamingData?.adaptiveFormats?.size ?: 0}"
        )
    }

    // Diagnostic-only summary of a player() failure. For an HTTP-shaped failure (a non-2xx
    // response from any of the client attempts), logs only the response's status code and the
    // request's host -- never the full request/response URL (which carries the API key and, for
    // a logged-in request, would otherwise risk exposing query parameters) and never headers.
    private fun logPlayerFailure(videoId: String, throwable: Throwable) {
        val summary = if (throwable is ResponseException) {
            "${throwable::class.simpleName} status=${throwable.response.status.value} host=${throwable.response.call.request.url.host}"
        } else {
            "${throwable::class.simpleName}: ${throwable.message?.take(200)}"
        }
        System.err.println("[InnerTube.player] videoId=$videoId FAILED -- $summary")
    }

    // Resolves a Piped audio stream list for videoId by trying each of PIPED_INSTANCES via the
    // pure selectFirstNonEmpty algorithm (see its own doc), wiring in the real network call and
    // this class's existing diagnostic logging (piped instance/result line, plus logPlayerFailure
    // for an exception -- same sanitized host+status-only summary already used elsewhere here).
    private suspend fun fetchPipedAudioStreams(videoId: String): List<PipedResponse.AudioStream> =
        selectFirstNonEmpty(
            instances = PIPED_INSTANCES,
            fetch = { instance -> innerTube.pipedStreams(instance, videoId).body<PipedResponse>().audioStreams },
        ) { instance, result ->
            result.onFailure { logPlayerFailure(videoId, it) }
            System.err.println("[InnerTube.player] piped instance=$instance videoId=$videoId streams=${result.getOrNull()?.size ?: 0}")
        }

    // WEB_REMIX request carrying a WebView-generated BotGuard PoToken (see the app module's
    // PoTokenGenerator, which is where the actual token generation happens -- this module stays
    // pure JVM/no-Android, so it only knows how to send a token it's handed, not how to make one).
    // Callers should try this before [player] and fall back to it on failure/an unplayable result,
    // exactly like the app module's MusicService does.
    suspend fun playerWithPoToken(videoId: String, playlistId: String? = null, poToken: String): Result<PlayerResponse> = runCatching {
        val response = innerTube.player(WEB_REMIX, videoId, playlistId, poToken).body<PlayerResponse>()
        logPlayerAttempt("WEB_REMIX", videoId, response)
        response
    }.onFailure { logPlayerFailure(videoId, it) }

    suspend fun player(videoId: String, playlistId: String? = null): Result<PlayerResponse> = runCatching {
        // ANDROID_MUSIC and IOS are each wrapped in their own runCatching: a hard failure (an
        // HTTP error or deserialization exception, not just a non-OK playabilityStatus) on one
        // client must fall through to the next client exactly like a non-OK status already did
        // -- otherwise a single client rejecting the request aborts the whole function via the
        // outer runCatching and skips the TVHTML5+piped fallback entirely, even though that
        // fallback exists specifically to recover from this kind of total failure. Confirmed via
        // the diagnostic logging below: IOS returning a hard HTTP 400 ("Precondition check
        // failed") was surfacing as "Unknown error" for every song, without TVHTML5 ever being
        // attempted.
        var playerResponse: PlayerResponse? = null
        if (this.cookie != null) { // if logged in: try ANDROID_MUSIC client first because IOS client does not play age restricted songs
            playerResponse = runCatching {
                innerTube.player(ANDROID_MUSIC, videoId, playlistId).body<PlayerResponse>()
            }.onSuccess { logPlayerAttempt("ANDROID_MUSIC", videoId, it) }
                .onFailure { logPlayerFailure(videoId, it) }
                .getOrNull()
            if (playerResponse?.playabilityStatus?.status == "OK" && playerResponse.hasPlayableAudioFormat()) {
                return@runCatching playerResponse
            }
        }
        val iosResponse = runCatching {
            innerTube.player(IOS, videoId, playlistId).body<PlayerResponse>()
        }.onSuccess { logPlayerAttempt("IOS", videoId, it) }
            .onFailure { logPlayerFailure(videoId, it) }
            .getOrNull()
        if (iosResponse != null) {
            playerResponse = iosResponse
            if (iosResponse.playabilityStatus.status == "OK" && iosResponse.hasPlayableAudioFormat()) {
                return@runCatching iosResponse
            }
        }
        val safePlayerResponse = innerTube.player(TVHTML5, videoId, playlistId).body<PlayerResponse>()
        logPlayerAttempt("TVHTML5", videoId, safePlayerResponse)
        if (safePlayerResponse.playabilityStatus.status == "OK") {
            val audioStreams = fetchPipedAudioStreams(videoId)
            return@runCatching safePlayerResponse.copy(
                streamingData = safePlayerResponse.streamingData?.copy(
                    adaptiveFormats = safePlayerResponse.streamingData.adaptiveFormats.mapNotNull { adaptiveFormat ->
                        audioStreams.find { it.bitrate == adaptiveFormat.bitrate }?.let {
                            adaptiveFormat.copy(
                                url = it.url
                            )
                        }
                    }
                )
            )
        }
        // TVHTML5 itself was rejected (confirmed on a real device: playabilityStatus.reason ==
        // "YouTube is no longer supported in this application or device", a rejection message
        // YouTube's own API returns for this client -- not something InnerTune produces). Piped
        // is a genuinely independent proxy: it resolves and serves YouTube's own audio stream
        // URLs itself and has no playability gate of its own tied to TVHTML5's request, so it
        // doesn't need TVHTML5 to have succeeded -- only the already-known videoId. Previously
        // this fallback was only ever used to patch URLs onto TVHTML5's own adaptiveFormats,
        // which don't exist here (YouTube omits streamingData for a rejected client), so a
        // rejected TVHTML5 silently skipped Piped entirely. Build formats directly from Piped's
        // own streams instead.
        val pipedAudioStreams = fetchPipedAudioStreams(videoId)
        if (pipedAudioStreams.isNotEmpty()) {
            return@runCatching safePlayerResponse.copy(
                playabilityStatus = safePlayerResponse.playabilityStatus.copy(status = "OK", reason = null),
                streamingData = PlayerResponse.StreamingData(
                    formats = null,
                    adaptiveFormats = pipedAudioStreams.map { it.toFormat() },
                    expiresInSeconds = PIPED_STREAM_ASSUMED_EXPIRY_SECONDS
                )
            )
        }
        playerResponse ?: safePlayerResponse
    }.onFailure { throwable ->
        logPlayerFailure(videoId, throwable)
    }

    suspend fun next(endpoint: WatchEndpoint, continuation: String? = null): Result<NextResult> = runCatching {
        val response = innerTube.next(WEB_REMIX, endpoint.videoId, endpoint.playlistId, endpoint.playlistSetVideoId, endpoint.index, endpoint.params, continuation).body<NextResponse>()
        val title = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0].tabRenderer.content?.musicQueueRenderer?.header?.musicQueueHeaderRenderer?.subtitle?.runs?.firstOrNull()?.text
        val playlistPanelRenderer = response.continuationContents?.playlistPanelContinuation
            ?: response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs[0].tabRenderer.content?.musicQueueRenderer?.content?.playlistPanelRenderer!!

        val items = playlistPanelRenderer.contents.mapNotNull { content ->
            content.playlistPanelVideoRenderer
                ?.let(NextPage::fromPlaylistPanelVideoRenderer)
                ?.let { it to content.playlistPanelVideoRenderer.selected }
        }
        val songs = items.map { it.first }
        val currentIndex = items.indexOfFirst { it.second }.takeIf { it != -1 }

        // load automix items
        playlistPanelRenderer.contents.lastOrNull()?.automixPreviewVideoRenderer?.content?.automixPlaylistVideoRenderer?.navigationEndpoint?.watchPlaylistEndpoint?.let { watchPlaylistEndpoint ->
            return@runCatching next(watchPlaylistEndpoint).getOrThrow().let { result ->
                result.copy(
                    title = title,
                    items = songs + result.items,
                    lyricsEndpoint = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.getOrNull(1)?.tabRenderer?.endpoint?.browseEndpoint,
                    relatedEndpoint = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.getOrNull(2)?.tabRenderer?.endpoint?.browseEndpoint,
                    currentIndex = currentIndex,
                    endpoint = watchPlaylistEndpoint
                )
            }
        }
        NextResult(
            title = title,
            items = songs,
            currentIndex = currentIndex,
            lyricsEndpoint = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.getOrNull(1)?.tabRenderer?.endpoint?.browseEndpoint,
            relatedEndpoint = response.contents.singleColumnMusicWatchNextResultsRenderer.tabbedRenderer.watchNextTabbedResultsRenderer.tabs.getOrNull(2)?.tabRenderer?.endpoint?.browseEndpoint,
            continuation = playlistPanelRenderer.continuations?.getContinuation(),
            endpoint = endpoint
        )
    }

    suspend fun lyrics(endpoint: BrowseEndpoint): Result<String?> = runCatching {
        val response = innerTube.browse(WEB_REMIX, endpoint.browseId, endpoint.params).body<BrowseResponse>()
        response.contents?.sectionListRenderer?.contents?.firstOrNull()?.musicDescriptionShelfRenderer?.description?.runs?.firstOrNull()?.text
    }

    suspend fun related(endpoint: BrowseEndpoint): Result<RelatedPage> = runCatching {
        val response = innerTube.browse(WEB_REMIX, endpoint.browseId).body<BrowseResponse>()
        val songs = mutableListOf<SongItem>()
        val albums = mutableListOf<AlbumItem>()
        val artists = mutableListOf<ArtistItem>()
        val playlists = mutableListOf<PlaylistItem>()
        response.contents?.sectionListRenderer?.contents?.forEach { sectionContent ->
            sectionContent.musicCarouselShelfRenderer?.contents?.forEach { content ->
                when (val item = content.musicResponsiveListItemRenderer?.let(RelatedPage.Companion::fromMusicResponsiveListItemRenderer)
                    ?: content.musicTwoRowItemRenderer?.let(RelatedPage.Companion::fromMusicTwoRowItemRenderer)) {
                    is SongItem -> if (content.musicResponsiveListItemRenderer?.overlay
                            ?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchEndpoint?.watchEndpointMusicSupportedConfigs
                            ?.watchEndpointMusicConfig?.musicVideoType == MUSIC_VIDEO_TYPE_ATV
                    ) songs.add(item)

                    is AlbumItem -> albums.add(item)
                    is ArtistItem -> artists.add(item)
                    is PlaylistItem -> playlists.add(item)
                    null -> {}
                }
            }
        }
        RelatedPage(songs, albums, artists, playlists)
    }

    suspend fun queue(videoIds: List<String>? = null, playlistId: String? = null): Result<List<SongItem>> = runCatching {
        if (videoIds != null) {
            assert(videoIds.size <= MAX_GET_QUEUE_SIZE) // Max video limit
        }
        innerTube.getQueue(WEB_REMIX, videoIds, playlistId).body<GetQueueResponse>().queueDatas
            .mapNotNull {
                it.content.playlistPanelVideoRenderer?.let { renderer ->
                    NextPage.fromPlaylistPanelVideoRenderer(renderer)
                }
            }
    }

    suspend fun transcript(videoId: String): Result<String> = runCatching {
        val response = innerTube.getTranscript(WEB, videoId).body<GetTranscriptResponse>()
        response.actions?.firstOrNull()?.updateEngagementPanelAction?.content?.transcriptRenderer?.body?.transcriptBodyRenderer?.cueGroups?.joinToString(separator = "\n") { group ->
            val time = group.transcriptCueGroupRenderer.cues[0].transcriptCueRenderer.startOffsetMs
            val text = group.transcriptCueGroupRenderer.cues[0].transcriptCueRenderer.cue.simpleText
                .trim('♪')
                .trim(' ')
            "[%02d:%02d.%03d]$text".format(time / 60000, (time / 1000) % 60, time % 1000)
        }!!
    }

    suspend fun visitorData(): Result<String> = runCatching {
        Json.parseToJsonElement(innerTube.getSwJsData().bodyAsText().substring(5))
            .jsonArray[0]
            .jsonArray[2]
            .jsonArray.first { (it as? JsonPrimitive)?.content?.startsWith(VISITOR_DATA_PREFIX) == true }
            .jsonPrimitive.content
    }

    suspend fun accountInfo(): Result<AccountInfo> = runCatching {
        innerTube.accountMenu(WEB_REMIX).body<AccountMenuResponse>()
            .actions[0].openPopupAction.popup.multiPageMenuRenderer
            .header?.activeAccountHeaderRenderer
            ?.toAccountInfo()!!
    }

    @JvmInline
    value class SearchFilter(val value: String) {
        companion object {
            val FILTER_SONG = SearchFilter("EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D")
            val FILTER_VIDEO = SearchFilter("EgWKAQIQAWoKEAkQChAFEAMQBA%3D%3D")
            val FILTER_ALBUM = SearchFilter("EgWKAQIYAWoKEAkQChAFEAMQBA%3D%3D")
            val FILTER_ARTIST = SearchFilter("EgWKAQIgAWoKEAkQChAFEAMQBA%3D%3D")
            val FILTER_FEATURED_PLAYLIST = SearchFilter("EgeKAQQoADgBagwQDhAKEAMQBRAJEAQ%3D")
            val FILTER_COMMUNITY_PLAYLIST = SearchFilter("EgeKAQQoAEABagoQAxAEEAoQCRAF")
        }
    }

    const val MAX_GET_QUEUE_SIZE = 1000

    private const val VISITOR_DATA_PREFIX = "Cgt"

    const val DEFAULT_VISITOR_DATA = "CgtsZG1ySnZiQWtSbyiMjuGSBg%3D%3D"
}
