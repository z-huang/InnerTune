package com.zionhuang.music

import android.content.Context
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeSearch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class YouTubeExtractorTest {
    @Test
    fun testSearch() {
        val context = mock(Context::class.java)
        val extractor = YouTubeExtractor.getInstance(context)
        val res: YouTubeSearch
        runBlocking {
            res = extractor.search("music")
        }
        assertTrue(res is YouTubeSearch.Success)
    }

    @Test
    fun testExtractId() {
        val context = mock(Context::class.java)
        val extractor = YouTubeExtractor.getInstance(context)
        val id = extractor.extractId("https://www.youtube.com/watch?v=4iRupuNet3Q?s=0")
        assertEquals("4iRupuNet3Q", id)
    }
}