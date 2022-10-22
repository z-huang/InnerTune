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
        getLyricsCandidate(title, artist, duration)?.let { candidate ->
            downloadLyrics(candidate.id, candidate.accesskey).content.decodeBase64String().normalize(normalizeTitle(title), normalizeArtist(artist))
        } ?: throw IllegalStateException("No lyrics candidate")
    }

    suspend fun getLyricsCandidate(title: String, artist: String, duration: Int): SearchLyricsResponse.Candidate? {
        val keyword = generateKeyword(title, artist)
        searchSongs(keyword).data.info.forEach { song ->
            if (abs(song.duration - duration) <= 8) {
                val candidate = searchLyricsByHash(song.hash).candidates.firstOrNull()
                if (candidate != null) return candidate
            }
        }
        return searchLyricsByKeyword(keyword, duration).candidates.firstOrNull()
    }

    private suspend fun searchSongs(keyword: String) = client.get("https://mobileservice.kugou.com/api/v3/search/song") {
        parameter("version", 9108)
        parameter("plat", 0)
        parameter("keyword", keyword)
    }.body<SearchSongResponse>()

    private suspend fun searchLyricsByKeyword(keyword: String, duration: Int) = client.get("https://lyrics.kugou.com/search") {
        parameter("ver", 1)
        parameter("man", "yes")
        parameter("client", "pc")
        parameter("keyword", keyword)
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

    private fun generateKeyword(title: String, artist: String): String {
        return "${normalizeTitle(title)} - ${normalizeArtist(artist)}"
    }

    private fun String.toSimplifiedChinese() = ZhConverterUtil.toSimple(this)
    private fun String.toTraditionalChinese(useTraditionalChinese: Boolean) = if (useTraditionalChinese) ZhConverterUtil.toTraditional(this) else this

    private fun String.normalize(title: String, artist: String): String = lines().filterNot { line ->
        line.endsWith("]") || BANNED_WORDS.any { line.contains(it) } || line.endsWith("].")
    }.let { lines ->
        val firstLine = lines.firstOrNull()?.toSimplifiedChinese() ?: return@let lines
        if (title.toSimplifiedChinese() in firstLine ||
            artist.split("、").any {
                it.toSimplifiedChinese() in firstLine
            }
        ) {
            lines.drop(1)
        } else lines
    }.joinToString(separator = "\n").toTraditionalChinese(useTraditionalChinese && none { c ->
        UnicodeScript.of(c.code) in JapaneseUnicodeScript
    })

    private val BANNED_WORDS = listOf(
        "]词:", "]词：", "]作词:", "]作词：",
        "]曲:", "]曲：", "]作曲:", "]作曲：",
        "]编曲：", "]编曲 Arrangement：",
        "]Producer：", "]制作人 Producer：",
        "]Drums：",
        "]Guitar：",
        "]Strings：",
        "]Mixer：",
        "]Mastering  Engineer："
    )

    private val JapaneseUnicodeScript = hashSetOf(
        UnicodeScript.HIRAGANA,
        UnicodeScript.KATAKANA,
    )
}
