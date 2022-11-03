package com.zionhuang.innertube.utils

import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import java.security.MessageDigest

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

fun sha1(str: String): String = MessageDigest.getInstance("SHA-1").digest(str.toByteArray()).toHex()

fun parseCookieString(cookie: String): Map<String, String> =
    cookie.split("; ").associate {
        val (key, value) = it.split("=")
        key to value
    }


fun isHTTP(url: URL): Boolean {
    // Make sure it's HTTP or HTTPS
    val protocol = url.protocol
    if (protocol != "http" && protocol != "https") {
        return false
    }
    val usesDefaultPort = url.port == url.defaultPort
    val setsNoPort = url.port == -1
    return setsNoPort || usesDefaultPort
}

fun isYoutubeURL(url: URL): Boolean {
    val host = url.host
    return host.equals("youtube.com", ignoreCase = true)
            || host.equals("www.youtube.com", ignoreCase = true)
            || host.equals("m.youtube.com", ignoreCase = true)
            || host.equals("music.youtube.com", ignoreCase = true)
}

/**
 * Get the value of a URL-query by name.
 *
 *
 *
 * If an url-query is give multiple times, only the value of the first query is returned.
 *
 *
 * @param url           the url to be used
 * @param parameterName the pattern that will be used to check the url
 * @return a string that contains the value of the query parameter or `null` if nothing
 * was found
 */
fun getQueryValue(
    url: URL,
    parameterName: String,
): String? {
    val urlQuery = url.query
    if (urlQuery != null) {
        for (param in urlQuery.split("&".toRegex())) {
            val params = param.split("=".toRegex(), 2)
            val query = try {
                URLDecoder.decode(params[0], "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                // Cannot decode string with UTF-8, using the string without decoding
                params[0]
            }
            if (query == parameterName) {
                return try {
                    URLDecoder.decode(params[1], "UTF-8")
                } catch (e: UnsupportedEncodingException) {
                    // Cannot decode string with UTF-8, using the string without decoding
                    params[1]
                }
            }
        }
    }
    return null
}

fun isYoutubeChannelMixId(playlistId: String): Boolean =
    playlistId.startsWith("RDCM")
