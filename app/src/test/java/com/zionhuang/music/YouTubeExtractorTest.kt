package com.zionhuang.music

import android.content.Context
import com.zionhuang.music.extractor.YouTubeExtractor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class YouTubeExtractorTest {
    @Test
    fun testSearch() {
        val context = mock(Context::class.java)
        val extractor = YouTubeExtractor.getInstance(context)
        val res: YouTubeExtractor.SearchResult
        runBlocking {
            res = extractor.search("music")
        }
        assertTrue(res is YouTubeExtractor.SearchResult.Success)
    }
}