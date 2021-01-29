package com.zionhuang.music

import android.content.Context
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeChannel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class YouTubeExtractorTest {
    private val context = mock(Context::class.java)
    private val extractor = YouTubeExtractor.getInstance(context)

    @Test
    fun testChannel() {
        val res: YouTubeChannel
        runBlocking {
            res = extractor.getChannel("UC-9-kyTW8ZkZNDHQJ6FgpwQ")
        }
        assertTrue(res is YouTubeChannel.Success)
    }

    @Test
    fun testExtractId() {
        val id = extractor.extractId("https://www.youtube.com/watch?v=4iRupuNet3Q?s=0")
        assertEquals("4iRupuNet3Q", id)
    }
}