package com.zionhuang.music.extractor

import android.util.Log
import androidx.collection.arrayMapOf
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.zionhuang.music.extensions.*
import com.zionhuang.music.extractor.ExtractorUtils.determineExt
import com.zionhuang.music.extractor.ExtractorUtils.mimeType2ext
import com.zionhuang.music.extractor.ExtractorUtils.parseCodecs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import kotlin.jvm.Throws

typealias SignatureFunctionKt = (String) -> String

/**
 * A Kotlin version of youtube_dl (https://github.com/ytdl-org/youtube-dl)
 *
 * @author Zion Huang
 * @version 2020.07.28
 */

object YouTubeExtractor {
    private const val TAG = "YouTubeExtractor"
    private val ASSETS_RE = arrayOf(
            """"assets":.+?"js":\s*"([^"]+)"""",
            """<script\s+src="([^"]+)".*name="player_ias\/base"\s*\/?>""" // get player url from script tag
    )
    private const val AGE_GATE_ASSETS_RE = """ytplayer\.config.*?"url"\s*:\s*("[^"]+")"""
    private val PlayerInfoRE = arrayOf(
            """/(?<id>[a-zA-Z0-9_-]{8,})/player_ias\.vflset(?:/[a-zA-Z]{2,3}_[a-zA-Z]{2,3})?/base\.(?<ext>[a-z]+)$""",
            """\b(?<id>vfl[a-zA-Z0-9_-]+)\b.*?\.(?<ext>[a-z]+)$"""
    )
    private const val WH_RE = """^(?<width>\d+)[xX](?<height>\d+)$"""
    private const val FILE_SIZE_RE = """\bclen[=/](\d+)"""
    private const val CODECS_RE = """(?<key>[a-zA-Z_-]+)=(?<quote>[\"\']?)(?<val>.+?)(?=quote)(?:;|$)"""
    private const val RATIO_RE = """<meta\s+property=\"og:video:tag\".*?content=\"yt:stretch=(?<w>[0-9]+):(?<h>[0-9]+)\">"""
    private val JS_FN_RE = arrayOf(
            """\b[cs]\s*&&\s*[adf]\.set\([^,]+\s*,\s*encodeURIComponent\s*\(\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\b[a-zA-Z0-9]+\s*&&\s*[a-zA-Z0-9]+\.set\([^,]+\s*,\s*encodeURIComponent\s*\(\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """(?:\b|[^a-zA-Z0-9$])(?<sig>[a-zA-Z0-9$]{2})\s*=\s*function\(\s*a\s*\)\s*\{\s*a\s*=\s*a\.split\(\s*""\s*\)""",
            """(?<sig>[a-zA-Z0-9$]+)\s*=\s*function\(\s*a\s*\)\s*\{\s*a\s*=\s*a\.split\(\s*\"\"\s*\)""",
            """(["\'])signature\1\s*,\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\.sig\|\|(?<sig>[a-zA-Z0-9$]+)\(""",
            """yt\.akamaized\.net/\)\s*\|\|\s*.*?\s*[cs]\s*&&\s*[adf]\.set\([^,]+\s*,\s*(?:encodeURIComponent\s*\()?\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\b[cs]\s*&&\s*[adf]\.set\([^,]+\s*,\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\b[a-zA-Z0-9]+\s*&&\s*[a-zA-Z0-9]+\.set\([^,]+\s*,\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\bc\s*&&\s*a\.set\([^,]+\s*,\s*\([^)]*\)\s*\(\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\bc\s*&&\s*[a-zA-Z0-9]+\.set\([^,]+\s*,\s*\([^)]*\)\s*\(\s*(?<sig>[a-zA-Z0-9$]+)\(""",
            """\bc\s*&&\s*[a-zA-Z0-9]+\.set\([^,]+\s*,\s*\([^)]*\)\s*\(\s*(?<sig>[a-zA-Z0-9$]+)\("""
    )

    private val playerCache = HashMap<String, SignatureFunctionKt>()

    sealed class Result {
        class Success(
                val id: String,
                val title: String,
                val channelTitle: String,
                val duration: Int,
                val formats: List<YtFormat>
        ) : Result()

        class Error(val errorCode: ErrorCode, val errorMessage: String) : Result()
    }

    private fun getYtPlayerConfig(videoWebPage: String): JsonObject? {
        val patterns = arrayOf(
                """;ytplayer\.config\s*=\s*(\{.+?\});ytplayer""",
                """;ytplayer\.config\s*=\s*(\{.+?\});"""
        )
        return videoWebPage.search(patterns)?.parseJsonString().asJsonObjectOrNull
    }

    suspend fun extract(videoId: String): Result = withContext(Dispatchers.Default) {
        fun debug(msg: String) = Log.d(TAG, "[$videoId] $msg")

        val videoWebPage = try {
            debug("Download web page")
            downloadWebPage("https://www.youtube.com/watch?v=$videoId&gl=US&hl=en&has_verified=1&bpctr=9999999999")
        } catch (e: IOException) {
            return@withContext Result.Error(ErrorCode.NETWORK, "Unable to download the web page of the video")
        }

        var playerUrl: String? = null

        val dashMpdList = ArrayList<String>()

        fun addDashMpd(videoInfo: JsonObject?) = videoInfo["dashmpd"].asStringOrNull?.let { if (it !in dashMpdList) dashMpdList += it }
        fun addDashMpdPr(playerResponse: JsonObject?) = playerResponse["streamingData"]["dashManifestUrl"].asStringOrNull?.let { if (it !in dashMpdList) dashMpdList += it }

        var isLive = false

        fun extractPlayerResponse(playerResponseStr: String?): JsonObject? =
                playerResponseStr?.parseJsonString().asJsonObjectOrNull?.also { addDashMpdPr(it) }

        var playerResponse: JsonObject? = null

        val ageGate: Boolean = videoWebPage.find("player-age-gate-content>") != null
        debug("Age gate: ${if (ageGate) "true" else "false"}")
        var videoInfo: JsonObject? = null
        var embedWebPage: String? = null
        if (ageGate) {
            embedWebPage = try {
                debug("Download embed web page")
                downloadWebPage("https://www.youtube.com/embed/$videoId")
            } catch (e: IOException) {
                return@withContext Result.Error(ErrorCode.NETWORK, "Unable to download the embed web page of the video")
            }
            val videoInfoWebPage = try {
                debug("Download video info")
                downloadWebPage("""https://www.youtube.com/get_video_info?video_id=$videoId&eurl=https%3A%2F%2Fyoutube.googleapis.com%2Fv%2F$videoId&sts=${embedWebPage?.find("\"sts\"\\s*:\\s*(\\d+)") ?: ""}""")
            } catch (e: IOException) {
                return@withContext Result.Error(ErrorCode.NETWORK, "Unable to download the info of the video")
            }
            videoInfo = videoInfoWebPage.parseQueryString()
            playerResponse = extractPlayerResponse(videoInfo["player_response"].asStringOrNull)
            addDashMpd(videoInfo)
        } else {
            val ytPlayerConfig = getYtPlayerConfig(videoWebPage)
            if (ytPlayerConfig != null) {
                val args: JsonObject? = ytPlayerConfig["args"]?.asJsonObjectOrNull
                if ("url_encoded_fmt_stream_map" in args && "ypc_vid" in args) {
                    videoInfo = args
                    addDashMpd(videoInfo)
                }
                if (videoInfo == null && "ypc_vid" in args) {
                    debug("Rental video")
                    return@withContext Result.Error(ErrorCode.RENTAL, "Rental video not supported")
                }
                if (args["livestream"].asStringOrNull == "1" || args["live_playback"].asNumberOrNull == 1) {
                    isLive = true
                }
                if (playerResponse == null) {
                    playerResponse = extractPlayerResponse(args["player_response"].asStringOrNull)
                    addDashMpdPr(playerResponse)
                }
            }
        }

        if (videoInfo == null && playerResponse == null) {
            Result.Error(ErrorCode.NO_INFO, "Unable to extract video data")
        }

        val videoDetails = playerResponse["videoDetails"].asJsonObjectOrNull

        if (!isLive) {
            isLive = videoDetails["isLive"].asBooleanOrNull ?: false
        }

        if ("ypc_video_rental_bar_text" in videoInfo && "author" !in videoInfo) {
            Result.Error(ErrorCode.RENTAL, "Rental video not supported")
        }

        val streamingFormats = JsonArray().apply {
            playerResponse["streamingData"]["formats"].asJsonArrayOrNull?.let { addAll(it) }
            playerResponse["streamingData"]["adaptiveFormats"].asJsonArrayOrNull?.let { addAll(it) }
        }
        val formats = ArrayList<YtFormat>()

        if (videoInfo["conn"].asStringOrNull?.startsWith("rtmp") == true) {
            formats.add(YtFormat(
                    formatId = "_rtmp",
                    protocol = "rtmp",
                    url = videoInfo["conn"][0]!!.asString
            ))
        } else if (!isLive && (streamingFormats.isNotEmpty() ||
                        videoInfo["url_encoded_fmt_stream_map"].asStringOrBlank.isNotEmpty() ||
                        videoInfo["adaptive_fmts"].asStringOrBlank.isNotEmpty())) {
            if ("rtmpe%3Dyes" in "${videoInfo["url_encoded_fmt_stream_map"][0].asStringOrBlank},${videoInfo["adaptive_fmts"][0].asStringOrBlank}") {
                Result.Error(ErrorCode.RTMPE, "rtmpe downloads are not supported")
            }
            val formatsSpec = HashMap<String, YtFormat>()
            if ("fmt_list" in videoInfo) {
                for (fmt in videoInfo["fmt_list"].asStringOrBlank.split(",")) {
                    val spec = fmt.split("/")
                    if (spec.size > 1) {
                        val wh = spec[1].split("x")
                        if (wh.size == 2) {
                            formatsSpec[spec[0]] = YtFormat(
                                    width = wh[0].toIntOrNull(),
                                    height = wh[1].toIntOrNull()
                            )
                        }
                    }
                }
            }
            for (fmt in streamingFormats) {
                if (fmt !is JsonObject) continue
                if ("itag" !in fmt) continue
                val itag = fmt["itag"].asStringOrNull ?: continue
                formatsSpec[itag] = YtFormat(
                        width = fmt["width"].asIntOrNull,
                        height = fmt["height"].asIntOrNull,
                        formatNote = (fmt["qualityLabel"] ?: fmt["quality"]).asStringOrNull,
                        fps = fmt["fps"].asIntOrNull,
                        tbr = if (itag != "43") (fmt["averageBitrate"]
                                ?: fmt["bitrate"]).asFloatOrNull?.div(1000) else null,
                        asr = fmt["audioSampleRate"].asFloatOrNull,
                        fileSize = fmt["contentLength"].asIntOrNull
                )
            }
            for (fmt in streamingFormats) {
                if (fmt !is JsonObject) continue
                if ("drmFamilies" in fmt || "drm_families" in fmt) continue
                var url = fmt["url"].asStringOrNull
                var urlData: JsonObject?
                var cipher: String? = null
                if (url == null) {
                    cipher = (fmt["cipher"] ?: fmt["signatureCipher"]).asStringOrNull
                            ?: continue
                    urlData = cipher.parseQueryString()
                    url = urlData["url"].asStringOrNull ?: continue
                } else {
                    urlData = try {
                        @Suppress("BlockingMethodInNonBlockingContext")
                        URL(fmt["url"].asStringOrNull).query.parseQueryString()
                    } catch (e: MalformedURLException) {
                        null
                    }
                }

                val streamType = urlData["stream_type"][0].asNumberOrNull
                if (streamType == 3) continue

                val formatId = (fmt["itag"] ?: urlData["itag"]).asStringOrNull ?: continue

                if (cipher != null) {
                    if ("s" in urlData) {
                        var jsPlayerJson = (if (ageGate) embedWebPage else videoWebPage)?.search(ASSETS_RE)
                        if (jsPlayerJson == null && !ageGate) {
                            if (embedWebPage == null) {
                                embedWebPage = try {
                                    debug("Download embed web page")
                                    downloadWebPage("https://www.youtube.com/embed/$videoId")
                                } catch (e: IOException) {
                                    return@withContext Result.Error(ErrorCode.NETWORK, "Unable to download the embed web page of the video")
                                }
                            }
                            jsPlayerJson = embedWebPage?.search(ASSETS_RE)
                        }
                        playerUrl = jsPlayerJson?.unescape()
                                ?: videoWebPage.search(AGE_GATE_ASSETS_RE)?.unescape()
                    }
                    if ("sig" in urlData) {
                        url += "&signature=${urlData["sig"].asStringOrBlank}"
                    } else if ("s" in urlData) {
                        val encryptedSig = urlData["s"].asStringOrNull ?: continue
                        val signature = try {
                            decryptSignature(encryptedSig, playerUrl)
                        } catch (e: ExtractException) {
                            debug("Failed to decrypt signature: ${e.msg}")
                            continue
                        }
                        debug("decrypted signature: $signature")
                        url += "&${urlData["sp"].asStringOrNull ?: "signature"}=$signature"
                    }
                }
                if ("ratebypass" !in url) url += "&ratebypass=yes"
                val whLst = urlData["size"].asStringOrBlank.find(WH_RE, "width", "height")
                val type = (urlData["type"] ?: fmt["mimeType"]).asStringOrNull
                val (ext, vcodec, acodec) = extractExtAndCodecs(type)
                formats += (FORMATS[formatId] + formatsSpec[formatId] + YtFormat(
                        formatId = formatId,
                        url = url,
                        width = whLst?.get(0)?.toIntOrNull() ?: fmt["width"].asIntOrNull,
                        height = whLst?.get(1)?.toIntOrNull() ?: fmt["height"].asIntOrNull,
                        fileSize = urlData["clen"].asIntOrNull ?: extractFileSize(url),
                        formatNote = (urlData["quality_label"] ?: fmt["qualityLabel"]
                        ?: urlData["quality"] ?: fmt["quality"]).asStringOrNull,
                        tbr = if (formatId != "43") (urlData["bitrate"]
                                ?: fmt["bitrate"]).asFloatOrNull else null,
                        fps = (urlData["fps"] ?: fmt["fps"]).asIntOrNull,
                        ext = ext ?: determineExt(url),
                        vcodec = vcodec,
                        acodec = acodec
                ))!!
            }
        } else {
            TODO("implement m3u8")
        }

        val videoTitle = videoInfo["title"].asStringOrNull
                ?: videoDetails["title"].asStringOrNull
                ?: return@withContext Result.Error(ErrorCode.NO_INFO, "Can't extract video title")
        val channelTitle = videoInfo["author"].asStringOrNull
                ?: videoDetails["author"].asStringOrNull
                ?: return@withContext Result.Error(ErrorCode.NO_INFO, "Can't extract channel title")
        val duration = videoInfo["length_seconds"].asIntOrNull
                ?: videoDetails["lengthSeconds"].asIntOrNull
                ?: return@withContext Result.Error(ErrorCode.NO_INFO, "Can't extract video duration")

        // TODO: look for DASH manifest

        // Check for malformed aspect ratio
        videoWebPage.find(RATIO_RE, "w", "h")?.let { l ->
            val (w, h) = l.map { it?.toFloatOrNull() ?: return@let }
            if (w > 0 && h > 0) {
                val ratio = w / h
                for (fmt in formats) {
                    if (fmt.vcodec == "none") {
                        fmt.stretchedRatio = ratio
                    }
                }
            }
        }

        if (formats.isEmpty() && "license_info" in videoInfo || "licenseInfos" in playerResponse["streamingData"].asJsonObjectOrNull) {
            Result.Error(ErrorCode.DRM, "The video is drm protected")
        }

        Result.Success(
                id = videoId,
                title = videoTitle,
                channelTitle = channelTitle,
                duration = duration,
                formats = formats
        )
    }

    private fun parseSigJs(jsCode: String): SignatureFunctionKt {
        val funcName = jsCode.find(JS_FN_RE, "sig")!!
        val jsi = JsInterpreter(jsCode)
        val initialFn = jsi.extractFunction(funcName)
        return { s -> initialFn(jsonArrayOf(s)).asString }
    }

    @Throws(ExtractException::class)
    private fun extractPlayerInfo(playerUrl: String) = playerUrl.find(PlayerInfoRE, "ext", "id")
            ?: throw ExtractException("Cannot identify player $playerUrl")

    @Throws(ExtractException::class)
    private suspend fun extractSignatureFunction(playerUrl: String, exampleSig: String): SignatureFunctionKt {
        val (playerType, playerId) = extractPlayerInfo(playerUrl)
        if (playerType != "js") throw ExtractException("Unsupported player type: $playerType")

        val funcId = "$playerType$$playerId${signatureCacheId(exampleSig)}"
        // TODO: load function from cache

        return try {
            parseSigJs(downloadPlainText(playerUrl))
        } catch (e: IOException) {
            throw ExtractException("Failed to download js player")
        }
    }

    private fun signatureCacheId(exampleSig: String) = exampleSig.split(".".toRegex()).joinToString(".")

    @Throws(ExtractException::class)
    private suspend fun decryptSignature(s: String, playerUrl: String?): String {
        if (playerUrl == null) throw ExtractException("Cannot decrypt signature without player url")
        var realPlayerUrl = playerUrl
        if (playerUrl.startsWith("//")) {
            realPlayerUrl = "https:$playerUrl"
        } else if (!playerUrl.matches("https?://".toRegex())) {
            realPlayerUrl = "https://www.youtube.com$playerUrl"
        }

        val playerId = "$playerUrl$${signatureCacheId(s)}"
        val func = playerCache[playerId] ?: extractSignatureFunction(realPlayerUrl, s).also {
            playerCache[playerId] = it
        }
        return func(s)
    }

    private fun extractFileSize(url: String): Int? = url.search(FILE_SIZE_RE)?.toIntOrNull()

    private fun extractExtAndCodecs(type: String?): Triple<String?, String?, String?> {
        if (type != null) {
            val typeSplit = type.split(";")
            val kindExt = typeSplit[0].split("/")
            if (kindExt.size == 2) {
                val kind = kindExt[0]
                val ext = mimeType2ext(typeSplit[0])
                if (kind == "audio" || kind == "video") {
                    for (match in type.findAll(CODECS_RE)) {
                        if (match.groupValue("key") == "codecs") {
                            val codecs = parseCodecs(match.groupValue("val"))
                            return Triple(ext, codecs.first, codecs.second)
                        }
                    }
                }
            }
        }
        return Triple(null, null, null)
    }

    private val FORMATS = arrayMapOf(
            "5" to YtFormat(ext = "flv", width = 400, height = 240, acodec = "mp3", abr = 64, vcodec = "h263"),
            "6" to YtFormat(ext = "flv", width = 450, height = 270, acodec = "mp3", abr = 64, vcodec = "h263"),
            "13" to YtFormat(ext = "3gp", acodec = "aac", abr = 64, vcodec = "mp4v"),
            "17" to YtFormat(ext = "3gp", width = 176, height = 144, acodec = "aac", abr = 24, vcodec = "mp4v"),
            "18" to YtFormat(ext = "mp4", width = 640, height = 360, acodec = "aac", abr = 96, vcodec = "h264"),
            "22" to YtFormat(ext = "mp4", width = 1280, height = 720, acodec = "aac", abr = 192, vcodec = "h264"),
            "34" to YtFormat(ext = "flv", width = 640, height = 360, acodec = "aac", abr = 128, vcodec = "h264"),
            "35" to YtFormat(ext = "flv", width = 854, height = 480, acodec = "aac", abr = 128, vcodec = "h264"),
            // itag 36 videos are either 320x180 (BaW_jenozKc) or 320x240 (__2ABJjxzNo), abr varies as well
            "36" to YtFormat(ext = "3gp", width = 320, acodec = "aac", vcodec = "mp4v"),
            "37" to YtFormat(ext = "mp4", width = 4096, height = 3072, acodec = "aac", abr = 192, vcodec = "h264"),
            "38" to YtFormat(ext = "mp4", width = 1920, height = 1080, acodec = "aac", abr = 192, vcodec = "h264"),
            "43" to YtFormat(ext = "webm", width = 640, height = 360, acodec = "vorbis", abr = 128, vcodec = "vp8"),
            "44" to YtFormat(ext = "webm", width = 854, height = 480, acodec = "vorbis", abr = 128, vcodec = "vp8"),
            "45" to YtFormat(ext = "webm", width = 1280, height = 720, acodec = "vorbis", abr = 192, vcodec = "vp8"),
            "46" to YtFormat(ext = "webm", width = 1920, height = 1080, acodec = "vorbis", abr = 192, vcodec = "vp8"),
            "59" to YtFormat(ext = "mp4", width = 854, height = 480, acodec = "aac", abr = 128, vcodec = "h264"),
            "78" to YtFormat(ext = "mp4", width = 854, height = 480, acodec = "aac", abr = 128, vcodec = "h264"),

            // 3D videos
            "82" to YtFormat(ext = "mp4", width = 360, formatNote = "3D", acodec = "aac", abr = 128, vcodec = "h264"),
            "83" to YtFormat(ext = "mp4", width = 480, formatNote = "3D", acodec = "aac", abr = 128, vcodec = "h264"),
            "84" to YtFormat(ext = "mp4", width = 720, formatNote = "3D", acodec = "aac", abr = 192, vcodec = "h264"),
            "85" to YtFormat(ext = "mp4", width = 1080, formatNote = "3D", acodec = "aac", abr = 192, vcodec = "h264"),
            "100" to YtFormat(ext = "webm", width = 360, formatNote = "3D", acodec = "vorbis", abr = 128, vcodec = "vp8"),
            "101" to YtFormat(ext = "webm", width = 480, formatNote = "3D", acodec = "vorbis", abr = 192, vcodec = "vp8"),
            "102" to YtFormat(ext = "webm", width = 720, formatNote = "3D", acodec = "vorbis", abr = 192, vcodec = "vp8"),

            // Apple HTTP Live Streaming
            "91" to YtFormat(ext = "mp4", height = 144, formatNote = "HLS", acodec = "aac", abr = 48, vcodec = "h264"),
            "92" to YtFormat(ext = "mp4", height = 240, formatNote = "HLS", acodec = "aac", abr = 48, vcodec = "h264"),
            "93" to YtFormat(ext = "mp4", height = 360, formatNote = "HLS", acodec = "aac", abr = 128, vcodec = "h264"),
            "94" to YtFormat(ext = "mp4", height = 480, formatNote = "HLS", acodec = "aac", abr = 128, vcodec = "h264"),
            "95" to YtFormat(ext = "mp4", height = 720, formatNote = "HLS", acodec = "aac", abr = 256, vcodec = "h264"),
            "96" to YtFormat(ext = "mp4", height = 1080, formatNote = "HLS", acodec = "aac", abr = 256, vcodec = "h264"),
            "132" to YtFormat(ext = "mp4", height = 240, formatNote = "HLS", acodec = "aac", abr = 48, vcodec = "h264"),
            "151" to YtFormat(ext = "mp4", height = 72, formatNote = "HLS", acodec = "aac", abr = 24, vcodec = "h264"),

            // DASH mp4 video
            "133" to YtFormat(ext = "mp4", height = 240, formatNote = "DASH video", vcodec = "h264"),
            "134" to YtFormat(ext = "mp4", height = 360, formatNote = "DASH video", vcodec = "h264"),
            "135" to YtFormat(ext = "mp4", height = 480, formatNote = "DASH video", vcodec = "h264"),
            "136" to YtFormat(ext = "mp4", height = 720, formatNote = "DASH video", vcodec = "h264"),
            "137" to YtFormat(ext = "mp4", height = 1080, formatNote = "DASH video", vcodec = "h264"),
            "138" to YtFormat(ext = "mp4", formatNote = "DASH video", vcodec = "h264"), // Height can vary (https://github.com/ytdl-org/youtube-dl/issues/4559)
            "160" to YtFormat(ext = "mp4", height = 144, formatNote = "DASH video", vcodec = "h264"),
            "212" to YtFormat(ext = "mp4", height = 480, formatNote = "DASH video", vcodec = "h264"),
            "264" to YtFormat(ext = "mp4", height = 1440, formatNote = "DASH video", vcodec = "h264"),
            "298" to YtFormat(ext = "mp4", height = 720, formatNote = "DASH video", vcodec = "h264", fps = 60),
            "299" to YtFormat(ext = "mp4", height = 1080, formatNote = "DASH video", vcodec = "h264", fps = 60),
            "266" to YtFormat(ext = "mp4", height = 2160, formatNote = "DASH video", vcodec = "h264"),

            // DASH mp4 audio
            "139" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "aac", abr = 48),
            "140" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "aac", abr = 128),
            "141" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "aac", abr = 256),
            "256" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "aac"),
            "258" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "aac"),
            "325" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "dtse"),
            "328" to YtFormat(ext = "m4a", formatNote = "DASH audio", acodec = "ec-3"),

            // DASH webm
            "167" to YtFormat(ext = "webm", height = 360, width = 640, formatNote = "DASH video", vcodec = "vp8"),
            "168" to YtFormat(ext = "webm", height = 480, width = 854, formatNote = "DASH video", vcodec = "vp8"),
            "169" to YtFormat(ext = "webm", height = 720, width = 1280, formatNote = "DASH video", vcodec = "vp8"),
            "170" to YtFormat(ext = "webm", height = 1080, width = 1920, formatNote = "DASH video", vcodec = "vp8"),
            "218" to YtFormat(ext = "webm", height = 480, width = 854, formatNote = "DASH video", vcodec = "vp8"),
            "219" to YtFormat(ext = "webm", height = 480, width = 854, formatNote = "DASH video", vcodec = "vp8"),
            "278" to YtFormat(ext = "webm", height = 144, formatNote = "DASH video", vcodec = "vp9"),
            "242" to YtFormat(ext = "webm", height = 240, formatNote = "DASH video", vcodec = "vp9"),
            "243" to YtFormat(ext = "webm", height = 360, formatNote = "DASH video", vcodec = "vp9"),
            "244" to YtFormat(ext = "webm", height = 480, formatNote = "DASH video", vcodec = "vp9"),
            "245" to YtFormat(ext = "webm", height = 480, formatNote = "DASH video", vcodec = "vp9"),
            "246" to YtFormat(ext = "webm", height = 480, formatNote = "DASH video", vcodec = "vp9"),
            "247" to YtFormat(ext = "webm", height = 720, formatNote = "DASH video", vcodec = "vp9"),
            "248" to YtFormat(ext = "webm", height = 1080, formatNote = "DASH video", vcodec = "vp9"),
            "271" to YtFormat(ext = "webm", height = 1440, formatNote = "DASH video", vcodec = "vp9"),
            // itag 272 videos are either 3840x2160 (e.g. RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            "272" to YtFormat(ext = "webm", height = 2160, formatNote = "DASH video", vcodec = "vp9"),
            "302" to YtFormat(ext = "webm", height = 720, formatNote = "DASH video", vcodec = "vp9", fps = 60),
            "303" to YtFormat(ext = "webm", height = 1080, formatNote = "DASH video", vcodec = "vp9", fps = 60),
            "308" to YtFormat(ext = "webm", height = 1440, formatNote = "DASH video", vcodec = "vp9", fps = 60),
            "313" to YtFormat(ext = "webm", height = 2160, formatNote = "DASH video", vcodec = "vp9"),
            "315" to YtFormat(ext = "webm", height = 2160, formatNote = "DASH video", vcodec = "vp9", fps = 60),

            // DASH webm audio
            "171" to YtFormat(ext = "webm", acodec = "vorbis", formatNote = "DASH audio", abr = 128),
            "172" to YtFormat(ext = "webm", acodec = "vorbis", formatNote = "DASH audio", abr = 256),

            // DASH webm audio with opus inside
            "249" to YtFormat(ext = "webm", formatNote = "DASH audio", acodec = "opus", abr = 50),
            "250" to YtFormat(ext = "webm", formatNote = "DASH audio", acodec = "opus", abr = 70),
            "251" to YtFormat(ext = "webm", formatNote = "DASH audio", acodec = "opus", abr = 160),

            // RTMP
            "rtmp" to YtFormat(protocol = "rtmp"),

            // av01 video only formats sometimes served with "unknown" codecs
            "394" to YtFormat(vcodec = "av01.0.05M.08"),
            "395" to YtFormat(vcodec = "av01.0.05M.08"),
            "396" to YtFormat(vcodec = "av01.0.05M.08"),
            "397" to YtFormat(vcodec = "av01.0.05M.08")
    )

    internal class ExtractException(val msg: String?) : Exception()

    enum class ErrorCode {
        NETWORK,
        NO_INFO,
        JSON,
        RENTAL,
        RTMPE,
        DRM,
        UNEXPECTED
    }
}
