package com.zionhuang.innertube

import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.innertube.models.WatchEndpoint
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test

@Ignore("IDK Why GitHub Action always runs the test with error")
class YouTubeTest {
    private val youTube = YouTube

    @Test
    fun `Check 'player' endpoint`() = runBlocking {
        VIDEO_IDS.forEach { videoId ->
            val playerResponse = youTube.player(videoId).getOrThrow()
            assertEquals(videoId, playerResponse.videoDetails.videoId)
        }
    }

    @Test
    fun `Check playable stream`() = runBlocking {
        VIDEO_IDS.forEach { videoId ->
            val playerResponse = youTube.player(videoId).getOrThrow()
            val format = playerResponse.streamingData!!.adaptiveFormats[0]
            val url = format.url
            println(url)
            val response = HttpClient(OkHttp).get(url) {
                headers {
                    append("Range", "bytes=0-10")
                }
            }
            assertTrue(response.status.isSuccess())
        }
    }

    @Test
    fun `Check 'search' endpoint`() = runBlocking {
        // Top result with radio link
        val searchAllTypeResult = youTube.searchSummary("musi").getOrThrow()
        assertTrue(searchAllTypeResult.summaries.size > 1)
        for (filter in listOf(
            FILTER_SONG,
            FILTER_VIDEO,
            FILTER_ALBUM,
            FILTER_ARTIST,
            FILTER_FEATURED_PLAYLIST,
            FILTER_COMMUNITY_PLAYLIST
        )) {
            val searchResult = youTube.search(SEARCH_QUERY, filter).getOrThrow()
            assertTrue(searchResult.items.isNotEmpty())
        }
    }

    @Test
    fun `Check search continuation`() = runBlocking {
        var count = 5
        var searchResult = youTube.search(SEARCH_QUERY, FILTER_SONG).getOrThrow()
        while (searchResult.continuation != null && count > 0) {
            searchResult.items.forEach {
                println(it.title)
            }
            searchResult = youTube.searchContinuation(searchResult.continuation!!).getOrThrow()
            count -= 1
        }
        searchResult.items.forEach {
            println(it.title)
        }
    }

    @Test
    fun `Check 'get_search_suggestion' endpoint`() = runBlocking {
        val suggestions = youTube.getSearchSuggestions(SEARCH_QUERY).getOrThrow()
        assertTrue(suggestions.queries.isNotEmpty())
    }

    @Test
    fun `Check 'browse' endpoint`() = runBlocking {
        var artist = youTube.browseArtist("UCI6B8NkZKqlFWoiC_xE-hzA").getOrThrow()
        assertTrue(artist.sections.isNotEmpty())
        artist = youTube.browseArtist("UCy2RKLxIOMOfGld_yBYEBLw").getOrThrow() // Artist that contains audiobook
        assertTrue(artist.sections.isNotEmpty())
        val album = youTube.browseAlbum("MPREb_oNAdr9eUOfS").getOrThrow()
        assertTrue(album.songs.isNotEmpty())
        val playlist = youTube.browsePlaylist("RDCLAK5uy_mHAEb33pqvgdtuxsemicZNu-5w6rLRweo").getOrThrow()
        assertTrue(playlist.songs.isNotEmpty())
    }

    @Test
    fun `Check 'next' endpoint`() = runBlocking {
        var nextResult = youTube.next(WatchEndpoint(videoId = "qivRUhepWVA", playlistId = "RDEMQWAKLFUHzBCn9nEsPHDYAw")).getOrThrow()
        assertTrue(nextResult.items.isNotEmpty())
        nextResult = youTube.next(WatchEndpoint(videoId = "jF4KKOsoyDs", playlistId = "PLaHh1PiehjvqOXm1J7b2QGy2iAvN84Azb")).getOrThrow()
        assertTrue(nextResult.items.isNotEmpty())
    }

    @Test
    fun `Check 'next' continuation`() = runBlocking {
        val videoId = "qivRUhepWVA"
        val playlistId = "RDEMQWAKLFUHzBCn9nEsPHDYAw"
        var count = 5
        var nextResult = youTube.next(WatchEndpoint(videoId = videoId, playlistId = playlistId)).getOrThrow()
        while (nextResult.continuation != null && count > 0) {
            nextResult.items.forEach {
                println(it.title)
            }
            nextResult = youTube.next(WatchEndpoint(videoId = videoId, playlistId = playlistId), nextResult.continuation).getOrThrow()
            count -= 1
        }
        nextResult.items.forEach {
            println(it.title)
        }
    }

    @Test
    fun `Check 'get_queue' endpoint`() = runBlocking {
        var queue = youTube.getQueue(videoIds = VIDEO_IDS).getOrThrow()
        assertTrue(queue.isNotEmpty())
        queue = youTube.getQueue(playlistId = PLAYLIST_ID).getOrThrow()
        assertTrue(queue.isNotEmpty())
    }

    @Test
    fun `Browse playlist`() = runBlocking {
        // This playlist has 2900 songs
        val playlistId = "PLtAw-mgfCzRwduBTjBHknz5U4_ZM4n6qm"
        var count = 5
        val playlistPage = YouTube.browsePlaylist(playlistId).getOrThrow()
        var songs = playlistPage.songs
        var continuation = playlistPage.songsContinuation
        while (count > 0) {
            songs.forEach {
                println(it.id)
            }
            if (continuation == null) break
            val continuationPage = YouTube.browsePlaylistContinuation(continuation).getOrThrow()
            songs = continuationPage.songs
            continuation = continuationPage.continuation
            count--
        }
    }

    @Test
    fun lyrics() = runBlocking {
        val nextResult = YouTube.next(WatchEndpoint(videoId = "NCC6lI0GGy0")).getOrThrow()
        val lyrics = YouTube.getLyrics(nextResult.lyricsEndpoint!!).getOrThrow()
        assertTrue(lyrics != null)
    }

    companion object {
        private val VIDEO_IDS = listOf(
            "4H-N260cPCg",
            "jF4KKOsoyDs"
//            "x8VYWazR5mE" Login required
        )

        private const val PLAYLIST_ID = "RDAMVM_WVXrDmm-P0"

        private const val SEARCH_QUERY = "YOASOBI"
    }
}
