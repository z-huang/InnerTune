package com.zionhuang.music.utils

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.zionhuang.music.extensions.parseJsonString
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.File
import java.nio.charset.StandardCharsets

object OkHttpDownloader {
    private const val TAG = "OkHttpDownloader"
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:68.0) Gecko/20100101 Firefox/68.0"
    val client = OkHttpClient()

    fun requestOf(url: String, headers: Map<String, String>? = null, data: String? = null): Request =
            Request.Builder().apply {
                url(url)
                if (data != null) {
                    method("POST", data.toByteArray(StandardCharsets.UTF_8).toRequestBody())
                }
                addHeader("User-Agent", USER_AGENT)
                headers?.forEach { (key, value) ->
                    header(key, value)
                }
            }.build()

    @Throws(DownloadException::class)
    fun downloadJson(url: String, headers: Map<String, String>? = null, data: String? = null): JsonElement? {
        val request = Request.Builder().apply {
            url(url)
            if (data != null) {
                method("POST", data.toByteArray(StandardCharsets.UTF_8).toRequestBody())
            }
            addHeader("User-Agent", USER_AGENT)
            headers?.forEach { (key, value) ->
                header(key, value)
            }
        }.build()


        return try {
            client.newCall(request).execute().body?.string()?.parseJsonString()
        } catch (e: IOException) {
            throw DownloadException(e.message)
        } catch (e: JsonParseException) {
            throw DownloadException(e.message)
        }
    }

    fun downloadJson(url: String, headers: Map<String, String>? = null, data: JsonObject): JsonElement? =
            downloadJson(url, headers, data.toString())

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun downloadFile(request: Request, destination: File) = withContext(IO) {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw DownloadException(response.message)
        }
        destination.parentFile?.mkdirs()
        destination.apply {
            createNewFile()
        }
        Log.d(TAG, destination.absolutePath)
        response.body?.byteStream()!!.use { inputStream ->
            destination.outputStream().use {
                inputStream.copyTo(it)
            }
        }
    }

    class DownloadException(override val message: String?) : Exception(message)
}