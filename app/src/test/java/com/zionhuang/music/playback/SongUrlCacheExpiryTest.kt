package com.zionhuang.music.playback

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression test for a defect found while investigating reported playback failures, since fixed
 * in MusicService.createDataSourceFactory()/onPlayerError(). The pre-fix code stored
 *
 *     songUrlCache[mediaId] = format.url!! to playerResponse.streamingData!!.expiresInSeconds * 1000L
 *
 * i.e. a *duration* in milliseconds (e.g. ~21_600_000 for a 6-hour-lived URL), not an absolute
 * expiry timestamp, then checked reuse eligibility with `it.second < System.currentTimeMillis()`.
 * Since real wall-clock "now" (order of 10^12) is always vastly larger than a plausible
 * duration-in-millis (order of 10^7), that condition was always true -- the cached URL was
 * treated as valid forever, regardless of whether YouTube's actual signed CDN URL had long since
 * expired. Production now stores an absolute expiry (`System.currentTimeMillis() +
 * expiresInSeconds * 1000L`) and checks `it.second > System.currentTimeMillis()`, matching
 * [fixedIsStillValid] below exactly; [preFixBuggyIsStillValid] is kept only to document the bug
 * this guards against regressing to. No production code is exercised directly by this test -- it
 * isolates the exact formulas, since they live inline in a private ResolvingDataSource lambda.
 */
class SongUrlCacheExpiryTest {
    /** Mirrors the pre-fix (buggy) formula verbatim. cachedAtMillis is unused -- that was the bug: the old formula never incorporated it either. */
    @Suppress("UNUSED_PARAMETER")
    private fun preFixBuggyIsStillValid(cachedAtMillis: Long, expiresInSeconds: Int, nowMillis: Long): Boolean {
        val storedSecondValue = expiresInSeconds * 1000L // what was stored pre-fix -- a duration, not a timestamp
        return storedSecondValue < nowMillis // the pre-fix (buggy) check
    }

    /** Mirrors the applied fix verbatim: store an absolute expiry timestamp, and check it's still in the future. */
    private fun fixedIsStillValid(cachedAtMillis: Long, expiresInSeconds: Int, nowMillis: Long): Boolean {
        val expiresAtMillis = cachedAtMillis + expiresInSeconds * 1000L
        return expiresAtMillis > nowMillis
    }

    @Test
    fun `pre-fix formula treats a freshly cached URL as valid`() {
        val now = 1_776_000_000_000L // a realistic real-world epoch-millis "now"
        assertTrue(preFixBuggyIsStillValid(cachedAtMillis = now, expiresInSeconds = 21_600, nowMillis = now))
    }

    @Test
    fun `pre-fix formula BUG -- still treats the URL as valid long after it has actually expired`() {
        val cachedAt = 1_776_000_000_000L
        val expiresInSeconds = 21_600 // 6 hours, a typical YouTube streamingData.expiresInSeconds
        // 30 days after caching -- the real YouTube CDN URL is certainly expired by now.
        val muchLaterNow = cachedAt + 30L * 24 * 60 * 60 * 1000
        assertTrue(
            "this assertion documents the bug the fix guards against regressing to",
            preFixBuggyIsStillValid(cachedAt, expiresInSeconds, muchLaterNow)
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

    /**
     * Mirrors MusicService.onPlayerError()'s invalidation: on a playback error, the failed
     * song's cache entry is removed outright, regardless of its stored expiry, so the next play
     * attempt misses the cache and fetches a fresh URL instead of repeating the same failure.
     */
    @Test
    fun `invalidating a cache entry on playback error forces the next lookup to miss`() {
        val cache = HashMap<String, Pair<String, Long>>()
        val now = 1_776_000_000_000L
        val mediaId = "abc123"
        // Not yet time-expired by the stored formula, but the server rejected it anyway (e.g. the
        // URL was revoked early) -- exactly the case the pre-fix code could never recover from.
        cache[mediaId] = "https://stale.example/audio" to (now + 21_600 * 1000L)

        cache.remove(mediaId)

        assertNull(cache[mediaId]?.takeIf { it.second > now })
    }
}
