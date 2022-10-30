package com.zionhuang.kugou

import com.github.houbb.opencc4j.util.ZhConverterUtil
import com.zionhuang.kugou.models.DownloadLyricsResponse
import com.zionhuang.kugou.models.SearchLyricsResponse
import com.zionhuang.kugou.models.SearchSongResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.lang.Character.UnicodeScript
import java.lang.Integer.min
import kotlin.math.abs

/**
 * KuGou Lyrics Library
 * Modified from [ViMusic](https://github.com/vfsfitvnm/ViMusic)
 */
object KuGou {
    var useTraditionalChinese: Boolean = false

    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                encodeDefaults = true
            }
            json(json)
            json(json, ContentType.Text.Html)
            json(json, ContentType.Text.Plain)
        }

        install(ContentEncoding) {
            gzip()
            deflate()
        }

        defaultRequest {
            url("https://krcs.kugou.com")
        }
    }

    suspend fun getLyrics(title: String, artist: String, duration: Int): Result<String> = runCatching {
        val keyword = generateKeyword(title, artist)
        getLyricsCandidate(keyword, duration)?.let { candidate ->
            downloadLyrics(candidate.id, candidate.accesskey).content.decodeBase64String().normalize(keyword)
        } ?: throw IllegalStateException("No lyrics candidate")
    }

    suspend fun getAllLyrics(title: String, artist: String, duration: Int): Result<List<String>> = runCatching {
        val keyword = generateKeyword(title, artist)
        val candidates = searchSongs(keyword).data.info
            .filter { abs(it.duration - duration) <= DURATION_TOLERANCE }
            .mapNotNull {
                searchLyricsByHash(it.hash).candidates.firstOrNull()
            } + searchLyricsByKeyword(keyword, duration).candidates
        candidates.map {
            downloadLyrics(it.id, it.accesskey).content.decodeBase64String().normalize(keyword)
        }
    }

    suspend fun getLyricsCandidate(keyword: Pair<String, String>, duration: Int): SearchLyricsResponse.Candidate? {
        searchSongs(keyword).data.info.forEach { song ->
            if (abs(song.duration - duration) <= DURATION_TOLERANCE) {
                val candidate = searchLyricsByHash(song.hash).candidates.firstOrNull()
                if (candidate != null) return candidate
            }
        }
        return searchLyricsByKeyword(keyword, duration).candidates.firstOrNull()
    }

    private suspend fun searchSongs(keyword: Pair<String, String>) = client.get("https://mobileservice.kugou.com/api/v3/search/song") {
        parameter("version", 9108)
        parameter("plat", 0)
        parameter("keyword", "${keyword.first} - ${keyword.second}")
    }.body<SearchSongResponse>()

    private suspend fun searchLyricsByKeyword(keyword: Pair<String, String>, duration: Int) = client.get("https://lyrics.kugou.com/search") {
        parameter("ver", 1)
        parameter("man", "yes")
        parameter("client", "pc")
        parameter("keyword", "${keyword.first} - ${keyword.second}")
        parameter("duration", duration * 1000)
    }.body<SearchLyricsResponse>()

    private suspend fun searchLyricsByHash(hash: String) = client.get("https://lyrics.kugou.com/search") {
        parameter("ver", 1)
        parameter("man", "yes")
        parameter("client", "pc")
        parameter("hash", hash)
    }.body<SearchLyricsResponse>()

    private suspend fun downloadLyrics(id: Long, accessKey: String) = client.get("https://lyrics.kugou.com/download") {
        parameter("fmt", "lrc")
        parameter("charset", "utf8")
        parameter("client", "pc")
        parameter("ver", 1)
        parameter("id", id)
        parameter("accesskey", accessKey)
    }.body<DownloadLyricsResponse>()

    private fun normalizeTitle(title: String) = title
        .replace("\\(.*\\)".toRegex(), "")
        .replace("（.*）".toRegex(), "")
        .replace("「.*」".toRegex(), "")
        .replace("『.*』".toRegex(), "")
        .replace("<.*>".toRegex(), "")
        .replace("《.*》".toRegex(), "")
        .replace("〈.*〉".toRegex(), "")
        .replace("＜.*＞".toRegex(), "")

    private fun normalizeArtist(artist: String) = artist
        .replace(", ", "、")
        .replace(" & ", "、")
        .replace(".", "")
        .replace("和", "、")
        .replace("\\(.*\\)".toRegex(), "")
        .replace("（.*）".toRegex(), "")

    private fun generateKeyword(title: String, artist: String) = normalizeTitle(title) to normalizeArtist(artist)

    private fun String.normalize(keyword: Pair<String, String>): String = lines().filter { line ->
        line matches ACCEPTED_REGEX
    }.let {
        // Remove useless information such as singer, writer, composer, guitar, etc.
        var cutLine = 0
        for (i in min(30, it.lastIndex) downTo 0) {
            if (it[i] matches BANNED_REGEX) {
                cutLine = i + 1
                break
            }
        }
        it.drop(cutLine)
    }.let { lines ->
        val firstLine = lines.firstOrNull()?.toSimplifiedChinese() ?: return@let lines
        val (title, artist) = keyword
        if (title.toSimplifiedChinese() in firstLine ||
            artist.split("、").any { it.toSimplifiedChinese() in firstLine }
        ) {
            lines.drop(1)
        } else lines
    }.joinToString(separator = "\n").let {
        if (useTraditionalChinese) it.normalizeForTraditionalChinese()
        else it
    }

    private fun String.normalizeForTraditionalChinese() =
        if (none { c -> UnicodeScript.of(c.code) in JapaneseUnicodeScript }) toTraditionalChinese()
        else this

    private fun String.toSimplifiedChinese() = ZhConverterUtil.toSimple(this)
    private fun String.toTraditionalChinese() = ZhConverterUtil.toTraditional(this)

    private val ACCEPTED_REGEX = "\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\].*".toRegex()
    private val BANNED_REGEX = ".+].+[:：].+".toRegex()

    private val JapaneseUnicodeScript = hashSetOf(
        UnicodeScript.HIRAGANA,
        UnicodeScript.KATAKANA,
    )

    private const val DURATION_TOLERANCE = 8
}
