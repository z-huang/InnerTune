import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.YouTube.EXPLORE_BROWSE_ID
import com.zionhuang.innertube.YouTube.HOME_BROWSE_ID
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.zionhuang.innertube.models.endpoint.BrowseEndpoint
import com.zionhuang.innertube.models.toAlbumInfo
import com.zionhuang.innertube.models.toArtistInfo
import com.zionhuang.innertube.models.toPlaylistInfo
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class YouTubeTest {
    private val youTube = YouTube

    @Test
    fun `Check 'player' endpoint`() = VIDEO_IDS.forEach { videoId ->
        runBlocking {
            val playerResponse = youTube.player(videoId)
            assertEquals(videoId, playerResponse.videoDetails.videoId)
        }
    }

    @Test
    fun `Check playable stream`() = VIDEO_IDS.forEach { videoId ->
        runBlocking {
            val playerResponse = youTube.player(videoId)
            val format = playerResponse.streamingData!!.adaptiveFormats[0]
            val url = format.url
            println(url)
            val response = HttpClient(CIO).get(url) {
                headers {
                    append("Range", "bytes=0-10")
                }
            }
            assertTrue(response.status.isSuccess())
        }
    }

    @Test
    fun `Check 'search' endpoint`() = runBlocking {
        val searchAllTypeResult = youTube.searchAllType(SEARCH_QUERY)
        assertTrue(searchAllTypeResult.sections.size > 1)
        for (filter in listOf(
            FILTER_SONG,
            FILTER_FEATURED_PLAYLIST,
            FILTER_VIDEO,
            FILTER_ALBUM,
            FILTER_COMMUNITY_PLAYLIST,
            FILTER_ARTIST
        )) {
            val searchResult = youTube.search(SEARCH_QUERY, filter)
            assertTrue(searchResult.items.isNotEmpty())
        }
    }

    @Test
    fun `Check search continuation`() = runBlocking {
        var count = 5
        var searchResult = youTube.search(SEARCH_QUERY, FILTER_SONG)
        while (searchResult.continuation != null && count > 0) {
            searchResult.items.forEach {
                println(it.title)
            }
            searchResult = youTube.search(YouTube.Continuation(searchResult.continuation!!))
            count -= 1
        }
        searchResult.items.forEach {
            println(it.title)
        }
    }

    @Test
    fun `Check 'get_search_suggestion' endpoint`() = runBlocking {
        val suggestions = youTube.getSearchSuggestions(SEARCH_QUERY)
        assertTrue(suggestions.isNotEmpty())
    }

    @Test
    fun `Check 'browse' endpoint`() = runBlocking {
        val artistInfo = youTube.browse(BrowseEndpoint("UCI6B8NkZKqlFWoiC_xE-hzA")).toArtistInfo()
        assertTrue(artistInfo.contents.isNotEmpty())
        val albumInfo = youTube.browse(BrowseEndpoint("MPREb_oNAdr9eUOfS")).toAlbumInfo()
        assertTrue(albumInfo.items.isNotEmpty())
        val playlistInfo = youTube.browse(BrowseEndpoint("VLRDCLAK5uy_mHAEb33pqvgdtuxsemicZNu-5w6rLRweo")).toPlaylistInfo()
        assertTrue(playlistInfo.subtitle.isNotEmpty())
        listOf(HOME_BROWSE_ID, EXPLORE_BROWSE_ID).forEach { browseId ->
            val result = youTube.browse(BrowseEndpoint(browseId)).toBrowseResult()
            assertTrue(result.sections.isNotEmpty())
        }
    }

    @Test
    fun `Check 'browse' continuation`() = runBlocking {
        var result = youTube.browse(BrowseEndpoint(HOME_BROWSE_ID)).toBrowseResult()
        while (result.continuation != null) {
            result = youTube.browse(YouTube.Continuation(result.continuation!!)).toBrowseResult()
        }
    }

    @Test
    fun `Check 'next' endpoint`() = runBlocking {
        val videoId = "qivRUhepWVA"
        val playlistId = "RDEMQWAKLFUHzBCn9nEsPHDYAw"
        val nextResult = youTube.getPlaylistItems(videoId = videoId, playlistId = playlistId)
        assertTrue(nextResult.items.isNotEmpty())
        val playlistSongInfo = youTube.getPlaylistSongInfo(videoId = VIDEO_IDS.random())
        assertNotNull(playlistSongInfo.lyricsEndpoint)
    }

    @Test
    fun `Check 'next' continuation`() = runBlocking {
        val videoId = "qivRUhepWVA"
        val playlistId = "RDEMQWAKLFUHzBCn9nEsPHDYAw"
        var count = 5
        var nextResult = youTube.getPlaylistItems(videoId = videoId, playlistId = playlistId)
        while (nextResult.continuation != null && count > 0) {
            nextResult.items.forEach {
                println(it.title)
            }
            nextResult = youTube.getPlaylistItems(videoId = videoId, playlistId = playlistId, continuation = nextResult.continuation)
            count -= 1
        }
        nextResult.items.forEach {
            println(it.title)
        }
    }

    @Test
    fun `Check 'get_queue' endpoint`() = runBlocking {
        var queue = youTube.getQueue(videoIds = VIDEO_IDS)
        assertTrue(queue[0].navigationEndpoint.watchEndpoint!!.videoId == VIDEO_IDS[0])
        queue = youTube.getQueue(playlistId = PLAYLIST_ID)
        assertTrue(queue.isNotEmpty())
    }

    companion object {
        private val VIDEO_IDS = listOf(
            "4H-N260cPCg",
//            "x8VYWazR5mE" Login required
        )

        private const val PLAYLIST_ID = "RDAMVM_WVXrDmm-P0"

        private const val SEARCH_QUERY = "YOASOBI"
    }
}