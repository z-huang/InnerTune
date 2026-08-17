package com.zionhuang.innertube

import com.zionhuang.innertube.models.ResponseContext
import com.zionhuang.innertube.models.response.PlayerResponse
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure, network-free tests for [hasPlayableAudioFormat] -- the gate added after a real device
 * showed IOS returning playabilityStatus.status == "OK" with 23 formats, none of which had a
 * usable url (most likely a signatureCipher-only or PoToken-gated format our
 * [PlayerResponse.StreamingData.Format] model doesn't resolve a url for). Without this gate,
 * YouTube.player() would short-circuit on that "OK" response and skip the TVHTML5+Piped fallback
 * that exists specifically to recover from an unusable response.
 */
class PlayableAudioFormatTest {
    private fun responseContext() = ResponseContext(visitorData = null, serviceTrackingParams = null)

    private fun playabilityStatus() = PlayerResponse.PlayabilityStatus(status = "OK", reason = null)

    private fun audioFormat(itag: Int, url: String?) = PlayerResponse.StreamingData.Format(
        itag = itag,
        url = url,
        mimeType = "audio/webm; codecs=\"opus\"",
        bitrate = 128_000,
        width = null,
        height = null,
        contentLength = null,
        quality = "medium",
        fps = null,
        qualityLabel = null,
        averageBitrate = 128_000,
        audioQuality = null,
        approxDurationMs = null,
        audioSampleRate = null,
        audioChannels = null,
        loudnessDb = null,
        lastModified = null,
    )

    private fun videoFormat(itag: Int, url: String?) = audioFormat(itag, url).copy(width = 1280, height = 720)

    private fun playerResponse(formats: List<PlayerResponse.StreamingData.Format>?) = PlayerResponse(
        responseContext = responseContext(),
        playabilityStatus = playabilityStatus(),
        playerConfig = null,
        streamingData = formats?.let {
            PlayerResponse.StreamingData(formats = null, adaptiveFormats = it, expiresInSeconds = 21_600)
        },
        videoDetails = null,
    )

    @Test
    fun `false when streamingData is null`() {
        assertFalse(playerResponse(formats = null).hasPlayableAudioFormat())
    }

    @Test
    fun `false when adaptiveFormats is empty`() {
        assertFalse(playerResponse(formats = emptyList()).hasPlayableAudioFormat())
    }

    @Test
    fun `false when every audio format has a null url -- the real-device IOS case`() {
        val formats = listOf(
            audioFormat(itag = 249, url = null),
            audioFormat(itag = 250, url = null),
            audioFormat(itag = 251, url = null),
        )
        assertFalse(playerResponse(formats).hasPlayableAudioFormat())
    }

    @Test
    fun `true when at least one audio format has a non-null url`() {
        val formats = listOf(
            audioFormat(itag = 249, url = null),
            audioFormat(itag = 251, url = "https://example.invalid/251"),
        )
        assertTrue(playerResponse(formats).hasPlayableAudioFormat())
    }

    @Test
    fun `a video format with a url does not count -- only audio formats matter`() {
        val formats = listOf(
            videoFormat(itag = 137, url = "https://example.invalid/137"),
            audioFormat(itag = 251, url = null),
        )
        assertFalse(playerResponse(formats).hasPlayableAudioFormat())
    }
}
