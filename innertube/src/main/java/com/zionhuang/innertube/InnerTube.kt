package com.zionhuang.innertube

import com.zionhuang.innertube.encoder.brotli
import com.zionhuang.innertube.models.Locale
import com.zionhuang.innertube.models.YouTubeClient
import com.zionhuang.innertube.models.body.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * Provide access to InnerTube endpoints.
 * For making HTTP requests, not parsing response.
 */
class InnerTube(
    private val locale: Locale,
) {
    @OptIn(ExperimentalSerializationApi::class)
    val httpClient = HttpClient(CIO) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            })
        }

        install(ContentEncoding) {
            brotli(1.0F)
            gzip(0.9F)
            deflate(0.8F)
        }

        //install(Logging)

        defaultRequest {
            url("https://music.youtube.com/youtubei/v1/")
        }
    }

    private fun HttpRequestBuilder.configYTClient(client: YouTubeClient) {
        contentType(ContentType.Application.Json)
        headers {
            append("X-Goog-Api-Format-Version", "1")
            append("X-YouTube-Client-Name", client.clientName)
            append("X-YouTube-Client-Version", client.clientVersion)
            if (client.referer != null) {
                append("Referer", client.referer)
            }
        }
        userAgent(client.userAgent)
        parameter("key", client.api_key)
        parameter("prettyPrint", false)
    }

    suspend fun search(
        client: YouTubeClient,
        query: String? = null,
        params: String? = null,
        continuation: String? = null,
    ) = httpClient.post("search") {
        configYTClient(client)
        setBody(SearchBody(
            context = client.toContext(locale),
            query = query,
            params = params
        ))
        parameter("continuation", continuation)
        parameter("ctoken", continuation)
    }

    suspend fun player(
        client: YouTubeClient,
        videoId: String,
        playlistId: String?,
    ) = httpClient.post("player") {
        configYTClient(client)
        setBody(PlayerBody(
            context = client.toContext(locale),
            videoId = videoId,
            playlistId = playlistId
        ))
    }

    suspend fun browse(
        client: YouTubeClient,
        browseId: String,
        params: String?,
        continuation: String?,
    ) = httpClient.post("browse") {
        configYTClient(client)
        setBody(BrowseBody(
            context = client.toContext(locale),
            browseId = browseId,
            params = params
        ))
    }

    suspend fun next(
        client: YouTubeClient,
        videoId: String,
        playlistId: String?,
        playlistSetVideoId: String?,
        index: Int?,
        params: String?,
        continuation: String? = null,
    ) = httpClient.post("next") {
        configYTClient(client)
        setBody(NextBody(
            context = client.toContext(locale),
            videoId = videoId,
            playlistId = playlistId,
            playlistSetVideoId = playlistSetVideoId,
            index = index,
            params = params,
            continuation = continuation
        ))
    }

    suspend fun getSearchSuggestions(
        client: YouTubeClient,
        input: String,
    ) = httpClient.post("music/get_search_suggestions") {
        configYTClient(client)
        setBody(GetSearchSuggestionsBody(
            context = client.toContext(locale),
            input = input
        ))
    }

    suspend fun getQueue(
        client: YouTubeClient,
        videoIds: List<String>?,
        playlistId: String?,
    ) = httpClient.post("music/get_queue") {
        configYTClient(client)
        setBody(GetQueueBody(
            context = client.toContext(locale),
            videoIds = videoIds,
            playlistId = playlistId
        ))
    }

    companion object {
        const val SONG_PARAM = "EgWKAQIIAWoMEAMQDhAEEAkQChAF"
        const val FEATURED_PLAYLIST_PARAM = "EgeKAQQoADgBagwQAxAOEAQQCRAKEAU%3D"
        const val VIDEO_PARAM = "EgWKAQIQAWoMEAMQDhAEEAkQChAF"
        const val ALBUM_PARAM = "EgWKAQIYAWoMEAMQDhAEEAkQChAF"
        const val COMMUNITY_PLAYLIST_PARAM = "EgeKAQQoAEABagwQAxAOEAQQCRAKEAU%3D"
        const val ARTIST_PARAM = "EgWKAQIgAWoMEAMQDhAEEAkQChAF"

        const val HOME_BROWSE_ID = "FEmusic_home"
        const val EXPLORE_BROWSE_ID = "FEmusic_explore"
    }
}