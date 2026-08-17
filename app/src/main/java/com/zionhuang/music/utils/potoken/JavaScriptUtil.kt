package com.zionhuang.music.utils.potoken

import org.json.JSONArray
import org.json.JSONObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Parses the raw challenge data obtained from the Create endpoint and returns an object that can be
 * embedded in a JavaScript snippet.
 *
 * Uses org.json (part of the Android SDK, always present -- no new dependency) rather than
 * kotlinx.serialization, which is only an `implementation`-scoped dependency of the innertube
 * module and isn't visible from here.
 */
fun parseChallengeData(rawChallengeData: String): String {
    val scrambled = JSONArray(rawChallengeData)

    val challengeData: JSONArray = if (scrambled.length() > 1 && scrambled.opt(1) is String) {
        val descrambled = descramble(scrambled.getString(1))
        JSONArray(descrambled)
    } else {
        scrambled.getJSONArray(0)
    }

    val messageId = challengeData.getString(0)
    val interpreterHash = challengeData.getString(3)
    val program = challengeData.getString(4)
    val globalName = challengeData.getString(5)
    val clientExperimentsStateBlob = challengeData.getString(7)

    val privateDoNotAccessOrElseSafeScriptWrappedValue = findFirstStringElement(challengeData, 1)
    val privateDoNotAccessOrElseTrustedResourceUrlWrappedValue = findFirstStringElement(challengeData, 2)

    val interpreterJavascript = JSONObject()
        .put("privateDoNotAccessOrElseSafeScriptWrappedValue", privateDoNotAccessOrElseSafeScriptWrappedValue ?: JSONObject.NULL)
        .put("privateDoNotAccessOrElseTrustedResourceUrlWrappedValue", privateDoNotAccessOrElseTrustedResourceUrlWrappedValue ?: JSONObject.NULL)

    return JSONObject()
        .put("messageId", messageId)
        .put("interpreterJavascript", interpreterJavascript)
        .put("interpreterHash", interpreterHash)
        .put("program", program)
        .put("globalName", globalName)
        .put("clientExperimentsStateBlob", clientExperimentsStateBlob)
        .toString()
}

/** Finds the first string element inside the array at [index], or null if that's not possible. */
private fun findFirstStringElement(challengeData: JSONArray, index: Int): String? {
    if (challengeData.isNull(index)) return null
    val wrapped = challengeData.optJSONArray(index) ?: return null
    for (i in 0 until wrapped.length()) {
        val value = wrapped.opt(i)
        if (value is String) return value
    }
    return null
}

/**
 * Parses the raw integrity token data obtained from the GenerateIT endpoint to a JavaScript
 * `Uint8Array` that can be embedded directly in JavaScript code, and a [Long] representing the
 * duration of this token in seconds.
 */
fun parseIntegrityTokenData(rawIntegrityTokenData: String): Pair<String, Long> {
    val integrityTokenData = JSONArray(rawIntegrityTokenData)
    return base64ToU8(integrityTokenData.getString(0)) to integrityTokenData.getLong(1)
}

/**
 * Converts a string (usually the identifier used as input to `obtainPoToken`) to a JavaScript
 * `Uint8Array` that can be embedded directly in JavaScript code.
 */
fun stringToU8(identifier: String): String {
    return newUint8Array(identifier.toByteArray())
}

/**
 * Takes a poToken encoded as a sequence of bytes represented as integers separated by commas
 * (e.g. "97,98,99" would be "abc"), which is the output of `Uint8Array::toString()` in JavaScript,
 * and converts it to the specific base64 representation for poTokens.
 */
@OptIn(ExperimentalEncodingApi::class)
fun u8ToBase64(poToken: String): String {
    val bytes = poToken.split(",")
        .map { it.toUByte().toByte() }
        .toByteArray()
    return Base64.Default.encode(bytes)
        .replace("+", "-")
        .replace("/", "_")
}

/**
 * Takes the scrambled challenge, decodes it from base64, adds 97 to each byte.
 */
private fun descramble(scrambledChallenge: String): String {
    return base64ToByteString(scrambledChallenge)
        .map { (it + 97).toByte() }
        .toByteArray()
        .decodeToString()
}

/**
 * Decodes a base64 string encoded in the specific base64 representation used by YouTube, and
 * returns a JavaScript `Uint8Array` that can be embedded directly in JavaScript code.
 */
private fun base64ToU8(base64: String): String {
    return newUint8Array(base64ToByteString(base64))
}

private fun newUint8Array(contents: ByteArray): String {
    return "new Uint8Array([" + contents.joinToString(separator = ",") { it.toUByte().toString() } + "])"
}

/**
 * Decodes a base64 string encoded in the specific base64 representation used by YouTube.
 */
@OptIn(ExperimentalEncodingApi::class)
private fun base64ToByteString(base64: String): ByteArray {
    val base64Mod = base64
        .replace('-', '+')
        .replace('_', '/')
        .replace('.', '=')

    return try {
        Base64.Default.decode(base64Mod)
    } catch (e: IllegalArgumentException) {
        throw PoTokenException("Cannot base64 decode")
    }
}
