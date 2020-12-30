package com.zionhuang.music.utils

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.zionhuang.music.extensions.parseJsonString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.nio.charset.StandardCharsets

object OkHttpDownloader {
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:68.0) Gecko/20100101 Firefox/68.0"
    val client = OkHttpClient()

    @Throws(DownloadException::class)
    fun downloadJson(url: String, headers: Map<String, String>, data: String? = null): JsonElement? {
        val request = Request.Builder()
                .url(url)
                .method("GET", data?.toByteArray(StandardCharsets.UTF_8)?.toRequestBody())
                .addHeader("User-Agent", USER_AGENT)
                .apply {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                }
                .build()

        return try {
            client.newCall(request).execute().body?.string()?.parseJsonString()
        } catch (e: IOException) {
            throw DownloadException(e.message)
        } catch (e: JsonParseException) {
            throw DownloadException(e.message)
        }
    }

    class DownloadException(override val message: String?) : Exception(message)
}