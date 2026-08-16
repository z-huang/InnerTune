package com.zionhuang.music.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Diagnostic test, not a component of a fix. Proves a defect found while investigating reported
 * playback failures: MusicService.createDataSourceFactory()'s in-memory `songUrlCache` stores
 *
 *     songUrlCache[mediaId] = format.url!! to playerResponse.streamingData!!.expiresInSeconds * 1000L
 *
 * i.e. a *duration* in milliseconds (e.g. ~21_600_000 for a 6-hour-lived URL), not an absolute
 * expiry timestamp, then later checks reuse eligibility with:
 *
 *     songUrlCache[mediaId]?.takeIf { it.second < System.currentTimeMillis() }
 *
 * Since the real wall-clock "now" (System.currentTimeMillis(), on the order of 10^12 for any
 * real device date) is always vastly larger than a plausible duration-in-millis (on the order of
 * 10^7), this condition is always true -- the cached URL is treated as valid forever, regardless
 * of whether YouTube's actual signed CDN URL has long since expired. This is a strong candidate
 * explanation for intermittent "ExoPlayer source" playback failures on replay of songs cached
 * earlier in a long-running session: see the accompanying investigation report for details and
 * the recommended fix (store an absolute expiry timestamp instead). No production code is
 * changed by this test -- it isolates the exact formulas to prove the defect in isolation.
 */
class SongUrlCacheExpiryTest {
    /** Mirrors the current (buggy) formula verbatim. cachedAtMillis is unused -- that's the bug: the real formula never incorporates it either. */
    @Suppress("UNUSED_PARAMETER")
    private fun currentIsStillConsideredValid(cachedAtMillis: Long, expiresInSeconds: Int, nowMillis: Long): Boolean {
        val storedSecondValue = expiresInSeconds * 1000L // what's actually stored today -- a duration, not a timestamp
        return storedSecondValue < nowMillis // the current (buggy) check
    }

    /** The recommended fix: store an absolute expiry timestamp, and check it's still in the future. */
    private fun fixedIsStillValid(cachedAtMillis: Long, expiresInSeconds: Int, nowMillis: Long): Boolean {
        val expiresAtMillis = cachedAtMillis + expiresInSeconds * 1000L
        return expiresAtMillis > nowMillis
    }

    @Test
    fun `current formula treats a freshly cached URL as valid`() {
        val now = 1_776_000_000_000L // a realistic real-world epoch-millis "now"
        assertTrue(currentIsStillConsideredValid(cachedAtMillis = now, expiresInSeconds = 21_600, nowMillis = now))
    }

    @Test
    fun `current formula BUG -- still treats the URL as valid long after it has actually expired`() {
        val cachedAt = 1_776_000_000_000L
        val expiresInSeconds = 21_600 // 6 hours, a typical YouTube streamingData.expiresInSeconds
        // 30 days after caching -- the real YouTube CDN URL is certainly expired by now.
        val muchLaterNow = cachedAt + 30L * 24 * 60 * 60 * 1000
        assertTrue(
            "this assertion documents the bug: the current formula incorrectly still says 'valid'",
            currentIsStillConsideredValid(cachedAt, expiresInSeconds, muchLaterNow)
        )
    }

    @Test
    fun `fixed formula correctly treats a freshly cached URL as valid`() {
        val now = 1_776_000_000_000L
        assertTrue(fixedIsStillValid(cachedAtMillis = now, expiresInSeconds = 21_600, nowMillis = now))
    }

    @Test
    fun `fixed formula correctly treats an old URL as expired`() {
        val cachedAt = 1_776_000_000_000L
        val expiresInSeconds = 21_600
        val muchLaterNow = cachedAt + 30L * 24 * 60 * 60 * 1000
        assertFalse(fixedIsStillValid(cachedAt, expiresInSeconds, muchLaterNow))
    }

    @Test
    fun `fixed formula correctly treats a URL just before its expiry as still valid`() {
        val cachedAt = 1_776_000_000_000L
        val expiresInSeconds = 21_600
        val justBeforeExpiry = cachedAt + expiresInSeconds * 1000L - 1
        assertTrue(fixedIsStillValid(cachedAt, expiresInSeconds, justBeforeExpiry))
    }

    @Test
    fun `fixed formula correctly treats a URL just after its expiry as expired`() {
        val cachedAt = 1_776_000_000_000L
        val expiresInSeconds = 21_600
        val justAfterExpiry = cachedAt + expiresInSeconds * 1000L + 1
        assertFalse(fixedIsStillValid(cachedAt, expiresInSeconds, justAfterExpiry))
    }
}
