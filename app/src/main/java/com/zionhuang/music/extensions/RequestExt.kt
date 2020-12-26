package com.zionhuang.music.extensions

import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8

fun urlRequest(url: String, headers: Map<String, String>, data: String? = null): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
    doOutput = true
    headers.forEach { (key, value) -> setRequestProperty(key, value) }
    data?.let {
        outputStream.write(data.toByteArray(UTF_8))
    }
}

fun urlRequest(url: String, headers: Map<String, String>, data: JsonObject): HttpURLConnection = urlRequest(url, headers, data.toString())

suspend fun HttpURLConnection.get() = withContext(IO) {
    inputStream.bufferedReader().use(BufferedReader::readText)
}

val HttpURLConnection.inputReader: InputStreamReader get() = inputStream.reader()