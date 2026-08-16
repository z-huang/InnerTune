package com.zionhuang.music.utils.potoken

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Pure, network-free tests for the JSON/byte-format helpers behind PoToken generation. These are
 * the only parts of the PoToken subsystem testable without a real WebView + a real BotGuard
 * exchange with Google -- [PoTokenWebView]/[PoTokenGenerator] themselves require both and are not
 * unit-tested here; that gap is closed by manual WSA verification instead of a fake stand-in.
 */
class JavaScriptUtilTest {
    @Test
    fun `stringToU8 encodes each byte of the identifier as a Uint8Array literal`() {
        assertEquals("new Uint8Array([97,98,99])", stringToU8("abc"))
    }

    @Test
    fun `stringToU8 of an empty string produces an empty Uint8Array`() {
        assertEquals("new Uint8Array([])", stringToU8(""))
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun `u8ToBase64 round-trips back to the original bytes once url-safe chars are reversed`() {
        // Bytes chosen so the standard base64 alphabet would contain both '+' and '/', to
        // exercise the url-safe substitution (not just cases where it's a no-op).
        val bytes = byteArrayOf(0xFB.toByte(), 0xEF.toByte(), 0xBE.toByte(), 0x3F, 0x3E)
        val u8String = bytes.joinToString(",") { it.toUByte().toString() }

        val result = u8ToBase64(u8String)

        assertTrue("must not contain raw '+' after substitution: $result", '+' !in result)
        assertTrue("must not contain raw '/' after substitution: $result", '/' !in result)

        val standardBase64 = result.replace('-', '+').replace('_', '/')
        assertEquals(bytes.toList(), Base64.Default.decode(standardBase64).toList())
    }

    @Test
    fun `u8ToBase64 handles the full byte range including values above 127`() {
        val bytes = (0..255).map { it.toByte() }.toByteArray()
        val u8String = bytes.joinToString(",") { it.toUByte().toString() }
        // Must not throw, and the result must be non-empty.
        assertTrue(u8ToBase64(u8String).isNotEmpty())
    }

    @Test
    fun `parseIntegrityTokenData extracts the expiry seconds and decodes the token bytes`() {
        val tokenBytes = byteArrayOf(1, 2, 3, 4, 5)
        val tokenBase64 = encodeAsYouTubeBase64(tokenBytes)
        val raw = """["$tokenBase64", 21600]"""

        val (u8Literal, expirySeconds) = parseIntegrityTokenData(raw)

        assertEquals(21600L, expirySeconds)
        assertEquals("new Uint8Array([1,2,3,4,5])", u8Literal)
    }

    @Test
    fun `parseChallengeData extracts messageId, interpreterHash, program, globalName and blob`() {
        val raw = """[["msg-id-1", null, null, "hash123", "prog code", "globalVar", null, "blob123"]]"""

        val parsed = Json.parseToJsonElement(parseChallengeData(raw)).jsonObject

        assertEquals("msg-id-1", parsed["messageId"]?.jsonPrimitive?.content)
        assertEquals("hash123", parsed["interpreterHash"]?.jsonPrimitive?.content)
        assertEquals("prog code", parsed["program"]?.jsonPrimitive?.content)
        assertEquals("globalVar", parsed["globalName"]?.jsonPrimitive?.content)
        assertEquals("blob123", parsed["clientExperimentsStateBlob"]?.jsonPrimitive?.content)
    }

    @Test
    fun `parseChallengeData leaves interpreterJavascript wrapped values null when the source is null`() {
        val raw = """[["msg-id-1", null, null, "hash123", "prog code", "globalVar", null, "blob123"]]"""

        val interpreterJs = Json.parseToJsonElement(parseChallengeData(raw)).jsonObject["interpreterJavascript"]!!.jsonObject

        assertEquals(JsonNull, interpreterJs["privateDoNotAccessOrElseSafeScriptWrappedValue"])
        assertEquals(JsonNull, interpreterJs["privateDoNotAccessOrElseTrustedResourceUrlWrappedValue"])
    }

    @Test
    fun `parseChallengeData finds the first string element inside a wrapped-value array`() {
        val raw = """[["msg-id-1", [123, "the-actual-js-code", 456], null, "hash123", "prog code", "globalVar", null, "blob123"]]"""

        val interpreterJs = Json.parseToJsonElement(parseChallengeData(raw)).jsonObject["interpreterJavascript"]!!.jsonObject

        assertEquals(
            "the-actual-js-code",
            interpreterJs["privateDoNotAccessOrElseSafeScriptWrappedValue"]?.jsonPrimitive?.content
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encodeAsYouTubeBase64(bytes: ByteArray): String =
        Base64.Default.encode(bytes).replace('+', '-').replace('/', '_')
}
