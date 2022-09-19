package com.zionhuang.music.utils

import com.google.gson.JsonElement
import com.zionhuang.music.extensions.parseJsonString
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object OkHttpDownloader {
    private val client = OkHttpClient()

    @Throws(DownloadException::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun downloadJson(url: String): JsonElement = withContext(IO) {
        val request = Request.Builder().url(url).build()
        return@withContext client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw DownloadException(response.message)
            response.body!!.string().parseJsonString()
        }
    }

    class DownloadException(override val message: String?) : Exception(message)
}