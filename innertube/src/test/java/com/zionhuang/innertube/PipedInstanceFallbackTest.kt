package com.zionhuang.innertube

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure, network-free tests for [selectFirstNonEmpty] -- the retry/selection algorithm behind
 * YouTube.player()'s multi-instance Piped fallback (see fetchPipedAudioStreams there). No real
 * HTTP call or mocking library is involved: [fetch] is a plain fake lambda standing in for
 * "the response a given instance would return," which is exactly the shape a Piped API response
 * reduces to by the time it reaches this algorithm (a list, or a thrown exception).
 */
class PipedInstanceFallbackTest {
    private val instances = listOf("https://a.invalid", "https://b.invalid", "https://c.invalid")

    private class FakeServerFailure(message: String) : Exception(message)

    @Test
    fun `first instance succeeding returns immediately without trying the rest`() = runBlocking {
        val attempted = mutableListOf<String>()
        val result = selectFirstNonEmpty(
            instances = instances,
            fetch = { instance ->
                attempted += instance
                listOf("stream-from-$instance")
            }
        )
        assertEquals(listOf("stream-from-https://a.invalid"), result)
        assertEquals(listOf("https://a.invalid"), attempted)
    }

    @Test
    fun `first instance returning HTTP 526-style failure falls through to the second`() = runBlocking {
        val attempted = mutableListOf<String>()
        val result = selectFirstNonEmpty(
            instances = instances,
            fetch = { instance ->
                attempted += instance
                if (instance == "https://a.invalid") throw FakeServerFailure("HTTP 526") else listOf("ok")
            }
        )
        assertEquals(listOf("ok"), result)
        assertEquals(listOf("https://a.invalid", "https://b.invalid"), attempted)
    }

    @Test
    fun `first instance timing out falls through to the second`() = runBlocking {
        val attempted = mutableListOf<String>()
        val result = selectFirstNonEmpty(
            instances = instances,
            fetch = { instance ->
                attempted += instance
                if (instance == "https://a.invalid") throw java.util.concurrent.TimeoutException("timed out") else listOf("ok")
            }
        )
        assertEquals(listOf("ok"), result)
        assertEquals(listOf("https://a.invalid", "https://b.invalid"), attempted)
    }

    @Test
    fun `first instance returning another 5xx-style failure falls through to the second`() = runBlocking {
        val attempted = mutableListOf<String>()
        val result = selectFirstNonEmpty(
            instances = instances,
            fetch = { instance ->
                attempted += instance
                if (instance == "https://a.invalid") throw FakeServerFailure("HTTP 502") else listOf("ok")
            }
        )
        assertEquals(listOf("ok"), result)
        assertEquals(listOf("https://a.invalid", "https://b.invalid"), attempted)
    }

    @Test
    fun `all instances failing returns an empty list cleanly instead of throwing`() = runBlocking {
        val attempted = mutableListOf<String>()
        val result = selectFirstNonEmpty<String>(
            instances = instances,
            fetch = { instance ->
                attempted += instance
                throw FakeServerFailure("down")
            }
        )
        assertTrue(result.isEmpty())
        assertEquals(instances, attempted)
    }

    @Test
    fun `an instance responding successfully but with no streams for this video is treated like a failure`() = runBlocking {
        // Mirrors a real Piped/YouTube "not found for this video" response: the request itself
        // succeeds (no exception), it's just an empty result -- not a server/network failure, but
        // still not usable, so the algorithm must still move on to the next instance.
        val attempted = mutableListOf<String>()
        val result = selectFirstNonEmpty(
            instances = instances,
            fetch = { instance ->
                attempted += instance
                if (instance == "https://a.invalid") emptyList() else listOf("ok")
            }
        )
        assertEquals(listOf("ok"), result)
        assertEquals(listOf("https://a.invalid", "https://b.invalid"), attempted)
    }

    @Test
    fun `no infinite retry loop -- each instance is attempted at most once even when all fail`() = runBlocking {
        var callCount = 0
        selectFirstNonEmpty<String>(
            instances = instances,
            fetch = { callCount++; throw FakeServerFailure("down") }
        )
        assertEquals(instances.size, callCount)
    }

    @Test
    fun `duplicate instances in the list are not attempted twice`() = runBlocking {
        val attempted = mutableListOf<String>()
        val withDuplicate = listOf("https://a.invalid", "https://a.invalid", "https://b.invalid")
        selectFirstNonEmpty<String>(
            instances = withDuplicate,
            fetch = { instance ->
                attempted += instance
                throw FakeServerFailure("down")
            }
        )
        assertEquals(listOf("https://a.invalid", "https://b.invalid"), attempted)
    }

    @Test
    fun `onResult is called once per instance actually attempted, in order, and never after a successful short-circuit`() = runBlocking {
        val observed = mutableListOf<Pair<String, Boolean>>()
        selectFirstNonEmpty(
            instances = instances,
            fetch = { instance -> if (instance == "https://b.invalid") listOf("ok") else throw FakeServerFailure("down") },
            onResult = { instance, result -> observed += instance to result.isSuccess }
        )
        assertEquals(listOf("https://a.invalid" to false, "https://b.invalid" to true), observed)
    }

    @Test
    fun `PIPED_INSTANCES always keeps kavin_rocks included, per the explicit requirement to keep it`() {
        assertTrue(PIPED_INSTANCES.contains("https://pipedapi.kavin.rocks"))
    }

    @Test
    fun `PIPED_INSTANCES has no accidental duplicate entries`() {
        assertEquals(PIPED_INSTANCES.distinct(), PIPED_INSTANCES)
    }
}
