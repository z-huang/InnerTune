package com.zionhuang.innertube

import com.zionhuang.innertube.models.YouTubeClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pure, network-free tests for [buildServiceIntegrityDimensions] -- the logic deciding whether a
 * player() request carries a PoToken. Only WEB_REMIX (the one client with useWebPoTokens=true)
 * should ever get one, and only when a token was actually supplied.
 */
class ServiceIntegrityDimensionsTest {
    @Test
    fun `WEB_REMIX with a token gets serviceIntegrityDimensions`() {
        val result = buildServiceIntegrityDimensions(YouTubeClient.WEB_REMIX, "some-token")
        assertEquals("some-token", result?.poToken)
    }

    @Test
    fun `WEB_REMIX without a token gets no serviceIntegrityDimensions`() {
        assertNull(buildServiceIntegrityDimensions(YouTubeClient.WEB_REMIX, null))
    }

    @Test
    fun `a non-PoToken client never gets serviceIntegrityDimensions, even with a token supplied`() {
        assertNull(buildServiceIntegrityDimensions(YouTubeClient.IOS, "some-token"))
        assertNull(buildServiceIntegrityDimensions(YouTubeClient.ANDROID_MUSIC, "some-token"))
        assertNull(buildServiceIntegrityDimensions(YouTubeClient.TVHTML5, "some-token"))
        assertNull(buildServiceIntegrityDimensions(YouTubeClient.WEB, "some-token"))
    }

    @Test
    fun `useWebPoTokens is set only for WEB_REMIX among current clients`() {
        assertEquals(true, YouTubeClient.WEB_REMIX.useWebPoTokens)
        for (client in listOf(YouTubeClient.IOS, YouTubeClient.ANDROID_MUSIC, YouTubeClient.ANDROID, YouTubeClient.WEB, YouTubeClient.TVHTML5)) {
            assertEquals(false, client.useWebPoTokens)
        }
    }
}
