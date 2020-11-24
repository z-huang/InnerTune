package com.zionhuang.music.extractor

import com.zionhuang.music.extensions.rPartition
import com.zionhuang.music.extensions.rStrip
import com.zionhuang.music.extensions.strip
import java.util.*

object ExtractorUtils {
    private val VCODECS = arrayOf("avc1", "avc2", "avc3", "avc4", "vp9", "vp8", "hev1", "hev2", "h263", "h264", "mp4v", "hvc1", "av01", "theora")
    private val ACODECS = arrayOf("mp4a", "opus", "vorbis", "mp3", "aac", "ac-3", "ec-3", "eac3", "dtsc", "dtse", "dtsh", "dtsl")
    private val KNOWN_EXTS = arrayOf(
            "mp4", "m4a", "m4p", "m4b", "m4r", "m4v", "aac",
            "flv", "f4v", "f4a", "f4b",
            "webm", "ogg", "ogv", "oga", "ogx", "spx", "opus",
            "mkv", "mka", "mk3d",
            "avi", "divx",
            "mov",
            "asf", "wmv", "wma",
            "3gp", "3g2",
            "mp3",
            "flac",
            "ape",
            "wav",
            "f4f", "f4m", "m3u8", "smil")

    /**
     * Parse video codec and audio codec from a given string.
     *
     * @param codecsStr the string to parse codecs
     * @return a pair of [video codec] and [audio codec]
     */
    fun parseCodecs(codecsStr: String?): Pair<String?, String?> {
        if (codecsStr == null) return Pair(null, null)
        val splitCodecs = codecsStr
                .trim()
                .strip(",")
                .split(",")
                .filter { it.isNotEmpty() }
        var vcodec: String? = null
        var acodec: String? = null
        for (fullCodec in splitCodecs) {
            val codec = fullCodec.split(".").getOrNull(0)
            if (codec in VCODECS && vcodec == null) {
                vcodec = fullCodec
            }
            if (codec in ACODECS && acodec == null) {
                acodec = fullCodec
            }
        }
        vcodec = vcodec ?: splitCodecs.getOrNull(0)
        acodec = acodec ?: splitCodecs.getOrNull(1)
        return Pair(vcodec, acodec)
    }

    fun mimeType2ext(mt: String?): String? {
        if (mt == null) return null
        when (mt) {
            "audio/mp4" -> return "m4a"
            "audio/mpeg" -> return "mp3"
        }
        val res = mt.split("/").run {
            getOrNull(size - 1)
                    ?.split(";")
                    ?.getOrNull(0)
                    ?.trim()
                    ?.toLowerCase(Locale.ROOT)
        }
        return when (res) {
            "3gpp" -> "3gp"
            "smptett+xml" -> "tt"
            "ttaf+xml" -> "dfxp"
            "ttml+xml" -> "ttml"
            "x-flv" -> "flv"
            "x-mp4-fragmented" -> "mp4"
            "x-ms-sami" -> "sami"
            "x-ms-wmv" -> "wmv"
            "mpegurl" -> "m3u8"
            "x-mpegurl" -> "m3u8"
            "vnd.apple.mpegurl" -> "m3u8"
            "dash+xml" -> "mpd"
            "f4m+xml" -> "f4m"
            "hds+xml" -> "f4m"
            "vnd.ms-sstr+xml" -> "ism"
            "quicktime" -> "mov"
            "mp2t" -> "ts"
            else -> res
        }
    }

    fun determineExt(url: String?): String? {
        if (url == null || "." !in url) return null
        val guess = url
                .split("\\?".toRegex(), 2)
                .getOrNull(0)
                ?.rPartition('.')
                ?.second ?: return null
        return when {
            guess.matches("""^[A-Za-z0-9]+$""".toRegex()) -> guess
            guess.rStrip("/") in KNOWN_EXTS -> guess.rStrip("/")
            else -> null
        }
    }
}