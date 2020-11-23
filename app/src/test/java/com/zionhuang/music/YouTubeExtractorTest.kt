package com.zionhuang.music

import com.zionhuang.music.extractor.YouTubeExtractor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeExtractorTest {
    @Test
    fun test() = runBlocking {
        val result = YouTubeExtractor.extract("BaW_jenozKc")
        assertTrue(result is YouTubeExtractor.Result.Success)
        if (result !is YouTubeExtractor.Result.Success) return@runBlocking
        assertEquals(result.title, """youtube-dl test video "'/\ä↭𝕐""")
    }
}