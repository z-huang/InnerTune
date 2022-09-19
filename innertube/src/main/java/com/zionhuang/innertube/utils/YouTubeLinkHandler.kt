package com.zionhuang.innertube.utils

import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.regex.Pattern

object YouTubeLinkHandler {
    private val YOUTUBE_VIDEO_ID_REGEX_PATTERN = Pattern.compile("^([a-zA-Z0-9_-]{11})")
    private val SUBPATHS = listOf("embed/", "shorts/", "watch/", "v/", "w/")
    private val EXCLUDED_SEGMENTS = Pattern.compile("playlist|watch|attribution_link|watch_popup|embed|feed|select_site")

    /**
     * Convert a string to a [URL object][URL].
     *
     *
     *
     * Defaults to HTTP if no protocol is given.
     *
     *
     * @param url the string to be converted to a URL-Object
     * @return a [URL object][URL] containing the url
     */
    private fun stringToURL(url: String): URL =
        try {
            URL(url)
        } catch (e: MalformedURLException) {
            // If no protocol is given try prepending "https://"
            if (e.message == "no protocol: $url") URL("https://$url") else throw e
        }

    private fun extractVideoId(id: String): String? {
        val m = YOUTUBE_VIDEO_ID_REGEX_PATTERN.matcher(id)
        return if (m.find()) m.group(1) else null
    }

    private fun getIdFromSubpathsInPath(path: String): String? {
        for (subpath in SUBPATHS) {
            if (path.startsWith(subpath)) {
                val id = path.substring(subpath.length)
                return extractVideoId(id)
            }
        }
        return null
    }

    fun getVideoId(theUrlString: String): String? {
        var urlString = theUrlString
        try {
            val uri = URI(urlString)
            val scheme = uri.scheme
            if (scheme != null && (scheme == "vnd.youtube" || scheme == "vnd.youtube.launch")) {
                val schemeSpecificPart = uri.schemeSpecificPart
                urlString =
                    if (schemeSpecificPart.startsWith("//")) {
                        extractVideoId(schemeSpecificPart.substring(2))?.let { return it }
                        "https:$schemeSpecificPart"
                    } else {
                        extractVideoId(schemeSpecificPart) ?: return null
                    }
            }
        } catch (ignored: URISyntaxException) {
        }
        val url: URL = try {
            stringToURL(urlString)
        } catch (e: MalformedURLException) {
            return null
        }
        val host = url.host
        var path = url.path
        // remove leading "/" of URL-path if URL-path is given
        if (path.isNotEmpty()) {
            path = path.substring(1)
        }
        return when (host.uppercase()) {
            "WWW.YOUTUBE-NOCOOKIE.COM" -> if (path.startsWith("embed/")) extractVideoId(path.substring(6)) else null
            "YOUTUBE.COM", "WWW.YOUTUBE.COM", "M.YOUTUBE.COM", "MUSIC.YOUTUBE.COM" -> {
                getIdFromSubpathsInPath(path)?.let { return it }
                getQueryValue(url, "v")?.let {
                    extractVideoId(it)
                }
            }
            "Y2U.BE", "YOUTU.BE" -> {
                getQueryValue(url, "v")?.let { extractVideoId(it) }
                    ?: extractVideoId(path)
            }
            else -> null
        }
    }

    /**
     * Returns true if path conform to
     * custom short channel URLs like youtube.com/yourcustomname
     *
     * @param splitPath path segments array
     * @return true - if value conform to short channel URL, false - not
     */
    private fun isCustomShortChannelUrl(splitPath: List<String>): Boolean =
        splitPath.size == 1 && !EXCLUDED_SEGMENTS.matcher(splitPath[0]).matches()

    fun getChannelId(url: String): String? {
        try {
            val urlObj = stringToURL(url)
            var path = urlObj.path

            if (!isHTTP(urlObj) || !isYoutubeURL(urlObj)) {
                // the URL given is not a Youtube-URL
                return null
            }

            // remove leading "/"
            path = path.substring(1)
            var splitPath = path.split("/".toRegex())

            // Handle custom short channel URLs like youtube.com/yourcustomname
            if (isCustomShortChannelUrl(splitPath)) {
                path = "c/$path"
                splitPath = path.split("/".toRegex())
            }
            if (!path.startsWith("user/") && !path.startsWith("channel/") && !path.startsWith("c/")) {
                // the URL given is neither a channel nor an user
                return null
            }
            val id = splitPath.getOrNull(1)
            if (id == null || !id.matches("[A-Za-z0-9_-]+".toRegex())) {
                // The given id is not a Youtube-Video-ID
                return null
            }
            return id
        } catch (exception: Exception) {
            return null
        }
    }

    fun getPlaylistId(url: String): String? {
        try {
            val urlObj = stringToURL(url)
            if (!isHTTP(urlObj) || !isYoutubeURL(urlObj)) {
                // the url given is not a YouTube-URL
                return null
            }
            val path = urlObj.path
            if (path != "/watch" && path != "/playlist") {
                // the url given is neither a video nor a playlist URL
                return null
            }
            val listID = getQueryValue(urlObj, "list") ?: return null // the URL given does not include a playlist
            if (!listID.matches("[a-zA-Z0-9_-]{10,}".toRegex())) {
                // the list-ID given in the URL does not match the list pattern
                return null
            }
            if (isYoutubeChannelMixId(listID) && getQueryValue(urlObj, "v") == null) {
                // Video id can't be determined from the channel mix id.
                // See YoutubeParsingHelper#extractVideoIdFromMixId

                // Channel Mix without a video id are not supported
                return null
            }
            return listID
        } catch (exception: Exception) {
            return null
        }
    }

    fun getBrowseId(url: String): String? =
        if (url.startsWith("https://music.youtube.com/browse/")) {
            url.substring("https://music.youtube.com/browse/".length)
        } else {
            null
        }
}
