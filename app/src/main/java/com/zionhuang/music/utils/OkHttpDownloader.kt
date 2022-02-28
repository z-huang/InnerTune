package com.zionhuang.music.utils

import com.google.gson.JsonElement
import com.zionhuang.music.extensions.parseJsonString
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

object OkHttpDownloader {
    private const val TAG = "OkHttpDownloader"
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

    @Throws(DownloadException::class)
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun downloadFile(url: String, destination: File) = withContext(IO) {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw DownloadException(response.message)
            val inputStream = response.body!!.byteStream()
            val outputStream = destination.outputStream()
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        }
    }

    class DownloadException(override val message: String?) : Exception(message)
}