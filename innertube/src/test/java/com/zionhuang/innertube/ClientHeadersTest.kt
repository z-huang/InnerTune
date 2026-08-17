package com.zionhuang.innertube

import com.zionhuang.innertube.models.YouTubeClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure, network-free tests for [buildYtClientHeaders] -- the request-header construction used by
 * every InnerTube call. Root-cause regression guard: `X-YouTube-Client-Name` must carry the
 * numeric [YouTubeClient.clientId], not the string [YouTubeClient.clientName]. Sending the string
 * name (the previous behavior) produced a generic precondition-check rejection from
 * music.youtube.com's player endpoint, confirmed by cross-referencing OuterTune, Metrolist, and
 * yt-dlp's INNERTUBE_CLIENTS table, all of which send the numeric ID in this header.
 */
class ClientHeadersTest {
    @Test
    fun `X-YouTube-Client-Name header carries the numeric clientId, not the string clientName`() {
        val clients = listOf(
            YouTubeClient.ANDROID_MUSIC,
            YouTubeClient.ANDROID,
            YouTubeClient.WEB,
            YouTubeClient.WEB_REMIX,
            YouTubeClient.TVHTML5,
            YouTubeClient.IOS,
        )
        for (client in clients) {
            val headers = buildYtClientHeaders(client, visitorData = null).toMap()
            assertEquals(client.clientId, headers["X-YouTube-Client-Name"])
            assertFalse(
                "clientId must be numeric (was '${client.clientId}' for ${client.clientName})",
                client.clientId.any { !it.isDigit() }
            )
        }
    }

    @Test
    fun `known client IDs match the current InnerTube protocol values`() {
        // Cross-checked against OuterTune, Metrolist, and yt-dlp's INNERTUBE_CLIENTS table.
        assertEquals("3", YouTubeClient.ANDROID_MUSIC.clientId)
        assertEquals("3", YouTubeClient.ANDROID.clientId)
        assertEquals("1", YouTubeClient.WEB.clientId)
        assertEquals("67", YouTubeClient.WEB_REMIX.clientId)
        assertEquals("85", YouTubeClient.TVHTML5.clientId)
        assertEquals("5", YouTubeClient.IOS.clientId)
    }

    @Test
    fun `X-YouTube-Client-Version header carries the client's version string`() {
        val headers = buildYtClientHeaders(YouTubeClient.IOS, visitorData = null).toMap()
        assertEquals(YouTubeClient.IOS.clientVersion, headers["X-YouTube-Client-Version"])
    }

    @Test
    fun `visitorData is sent as X-Goog-Visitor-Id when present`() {
        val headers = buildYtClientHeaders(YouTubeClient.IOS, visitorData = "some-visitor-id").toMap()
        assertEquals("some-visitor-id", headers["X-Goog-Visitor-Id"])
    }

    @Test
    fun `visitorData header is omitted when null`() {
        val headers = buildYtClientHeaders(YouTubeClient.IOS, visitorData = null).toMap()
        assertFalse(headers.containsKey("X-Goog-Visitor-Id"))
    }

    @Test
    fun `referer header is only sent for clients that declare one`() {
        val withReferer = buildYtClientHeaders(YouTubeClient.WEB_REMIX, visitorData = null).toMap()
        assertEquals(YouTubeClient.WEB_REMIX.referer, withReferer["Referer"])

        val withoutReferer = buildYtClientHeaders(YouTubeClient.IOS, visitorData = null).toMap()
        assertFalse(withoutReferer.containsKey("Referer"))
    }

    @Test
    fun `x-origin is always music_youtube_com`() {
        val headers = buildYtClientHeaders(YouTubeClient.IOS, visitorData = null).toMap()
        assertEquals("https://music.youtube.com", headers["x-origin"])
    }

    @Test
    fun `no duplicate header names are produced`() {
        val headers = buildYtClientHeaders(YouTubeClient.WEB_REMIX, visitorData = "v")
        val names = headers.map { it.first }
        assertEquals(names.distinct(), names)
        assertTrue(names.isNotEmpty())
    }
}
