package com.zionhuang.music.playback

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.fail

/**
 * TEMPORARY, one-off diagnostic -- not a permanent regression test. Probes every officially
 * listed Piped instance (https://github.com/TeamPiped/documentation .../public-instances/index.md,
 * fetched 2026-08-16) from GitHub Actions' real, unrestricted network, since neither the sandbox
 * this was written in nor the app itself can otherwise tell which public instances are currently
 * healthy before picking a fallback list. Deliberately fails with a full summary so the result is
 * guaranteed to show up in the CI job log regardless of Gradle's default test-output verbosity;
 * this is intentional for this one run and this test is removed once the data is collected.
 */
class PipedInstanceProbe {
    @Test
    fun `probe official Piped instances for reachability`() = runBlocking {
        val instances = listOf(
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.leptons.xyz",
            "https://pipedapi.nosebs.ru",
            "https://pipedapi-libre.kavin.rocks",
            "https://piped-api.privacy.com.de",
            "https://pipedapi.adminforge.de",
            "https://api.piped.yt",
            "https://pipedapi.drgns.space",
            "https://pipedapi.owo.si",
            "https://pipedapi.ducks.party",
            "https://piped-api.codespace.cz",
            "https://pipedapi.reallyaweso.me",
            "https://api.piped.private.coffee",
            "https://pipedapi.darkness.services",
            "https://pipedapi.orangenet.cc",
        )
        // "Me at the zoo" -- the first video ever uploaded to YouTube, stable/unlikely to ever be
        // removed, small enough for a fast response either way.
        val testVideoId = "jNQXAC9IVRw"

        val client = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 8000
                connectTimeoutMillis = 8000
            }
        }
        val results = instances.map { instance ->
            val outcome = try {
                val response = client.get("$instance/streams/$testVideoId")
                "HTTP ${response.status.value}"
            } catch (e: Exception) {
                "${e::class.simpleName}: ${e.message?.take(150)}"
            }
            instance to outcome
        }
        client.close()

        fail(
            "Piped instance probe results (not a real failure -- see message):\n" +
                results.joinToString("\n") { (instance, outcome) -> "$instance -> $outcome" }
        )
    }
}
