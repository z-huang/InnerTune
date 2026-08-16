package com.zionhuang.innertube

import com.zionhuang.innertube.models.response.PipedResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure, network-free tests for the Piped-independent fallback added to YouTube.player() (see the
 * "TVHTML5 itself was rejected" branch there): when YouTube rejects the TVHTML5 client outright
 * (e.g. "YouTube is no longer supported in this application or device"), formats are now built
 * directly from Piped's own audio streams via [PipedResponse.AudioStream.toFormat], rather than
 * only ever being used to patch URLs onto TVHTML5's own (in that case nonexistent) adaptiveFormats.
 */
class PipedFallbackTest {
    private fun audioStream(itag: Int, bitrate: Int = 128_000, url: String = "https://example.invalid/$itag") =
        PipedResponse.AudioStream(itag = itag, url = url, bitrate = bitrate)

    @Test
    fun `known itags map to their real YouTube mimeType`() {
        assertEquals("audio/webm; codecs=\"opus\"", PIPED_AUDIO_ITAG_MIME_TYPES[251])
        assertEquals("audio/webm; codecs=\"opus\"", PIPED_AUDIO_ITAG_MIME_TYPES[250])
        assertEquals("audio/webm; codecs=\"opus\"", PIPED_AUDIO_ITAG_MIME_TYPES[249])
        assertEquals("audio/mp4; codecs=\"mp4a.40.2\"", PIPED_AUDIO_ITAG_MIME_TYPES[140])
        assertEquals("audio/mp4; codecs=\"mp4a.40.2\"", PIPED_AUDIO_ITAG_MIME_TYPES[141])
        assertEquals("audio/webm; codecs=\"vorbis\"", PIPED_AUDIO_ITAG_MIME_TYPES[171])
    }

    @Test
    fun `unknown itag has no entry in the table`() {
        assertEquals(null, PIPED_AUDIO_ITAG_MIME_TYPES[999999])
    }

    @Test
    fun `toFormat preserves itag, url and bitrate verbatim`() {
        val stream = audioStream(itag = 251, bitrate = 160_000, url = "https://example.invalid/audio.webm")
        val format = stream.toFormat()
        assertEquals(251, format.itag)
        assertEquals("https://example.invalid/audio.webm", format.url)
        assertEquals(160_000, format.bitrate)
        assertEquals(160_000, format.averageBitrate)
    }

    @Test
    fun `toFormat uses the known mimeType for a known itag`() {
        assertEquals("audio/webm; codecs=\"opus\"", audioStream(itag = 251).toFormat().mimeType)
        assertEquals("audio/mp4; codecs=\"mp4a.40.2\"", audioStream(itag = 140).toFormat().mimeType)
    }

    @Test
    fun `toFormat falls back to opus webm for an unrecognized itag instead of throwing`() {
        val format = audioStream(itag = 999999).toFormat()
        assertEquals("audio/webm; codecs=\"opus\"", format.mimeType)
    }

    /**
     * Regression guard for MusicService.kt's FormatEntity storage step, which does
     * `format.mimeType.split(";")[0]` and `format.mimeType.split("codecs=")[1].removeSurrounding("\"")`
     * -- an IndexOutOfBoundsException there would crash playback resolution outright. Every
     * mimeType this table (and its fallback) can ever produce must survive both splits.
     */
    @Test
    fun `every possible toFormat mimeType is safe for MusicService's codec parsing`() {
        val allMimeTypes = PIPED_AUDIO_ITAG_MIME_TYPES.values + audioStream(itag = -1).toFormat().mimeType
        for (mimeType in allMimeTypes) {
            assertTrue("missing ';' in: $mimeType", mimeType.contains(";"))
            assertTrue("missing 'codecs=' in: $mimeType", mimeType.contains("codecs="))
            val codec = mimeType.split("codecs=")[1].removeSurrounding("\"")
            assertTrue("empty codec parsed from: $mimeType", codec.isNotEmpty())
        }
    }

    @Test
    fun `toFormat leaves fields Piped does not report as null`() {
        val format = audioStream(itag = 251).toFormat()
        assertNotNull(format.mimeType)
        assertEquals(null, format.width)
        assertEquals(null, format.height)
        assertEquals(null, format.contentLength)
        assertEquals(null, format.fps)
        assertEquals(null, format.qualityLabel)
        assertEquals(null, format.audioQuality)
        assertEquals(null, format.approxDurationMs)
        assertEquals(null, format.audioSampleRate)
        assertEquals(null, format.audioChannels)
        assertEquals(null, format.loudnessDb)
        assertEquals(null, format.lastModified)
    }

    @Test
    fun `PIPED_STREAM_ASSUMED_EXPIRY_SECONDS is a plausible positive duration`() {
        // Sanity bound only -- not tied to a specific value, just guards against an accidental
        // sign flip or unit mistake (e.g. milliseconds instead of seconds) breaking the
        // MusicService.kt songUrlCache expiry math (System.currentTimeMillis() + this * 1000L).
        assertTrue(PIPED_STREAM_ASSUMED_EXPIRY_SECONDS in 60..86_400)
    }
}
