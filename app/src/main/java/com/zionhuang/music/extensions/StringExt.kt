package com.zionhuang.music.extensions

import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.gson.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils.unescapeJava
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.util.regex.Pattern

operator fun String.get(begin: Int, end: Int) = this.substring(begin, end) // s[begin, end)

// Python-like replacement syntactic sugar
operator fun String.rem(replacement: String) = this.replaceFirst("%s", replacement)

fun String.escape(): String = Pattern.quote(this)
fun String.unescape(): String = unescapeJava(this)
fun String.removeQuotes(): String = when {
    length < 2 -> this
    first() == '\"' && last() == '\"' -> this[1, length - 1]
    first() == '\'' && last() == '\'' -> this[1, length - 1]
    else -> this
}

fun String.urlDecode(): String = URLDecoder.decode(this, "UTF8")

/**
 * Remove given characters at the beginning and the end of the string.
 */
fun String.strip(chars: String) = replace("""^[$chars]+|[$chars]+$""".toRegex(), "")
fun String.rStrip(chars: String) = replace("""[$chars]+$""".toRegex(), "")

/**
 * Search the given character from the end of the string.
 * @param sep the seperater
 * @return a [Triple] of the left part of [sep], [sep], and the right part of [sep] if [sep] is in the string, else [this], "", and "".
 */
fun String.rPartition(sep: Char): Triple<String, String, String> {
    for (i in length - 1 downTo 0) {
        if (this[i] == sep) {
            return Triple(substring(0, i), sep.toString(), substring(i + 1, length))
        }
    }
    return Triple(this, "", "")
}

fun String.trimLineStartSpaces() = lines().joinToString("") { it.trim() }

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun downloadWebPage(url: String): String = withContext(IO) {
    Jsoup.connect(url).timeout(60 * 1000).get().html()
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun downloadPlainText(url: String): String = withContext(IO) {
    Jsoup.connect(url).ignoreContentType(true).execute().body()
}

/**
 * JsonElement Extensions
 */
fun String.parseQueryString(): JsonObject = JsonObject().apply {
    for (pair in split("&")) {
        val idx = pair.indexOf("=")
        this[pair[0, idx].urlDecode()] = JsonPrimitive(pair[idx + 1, pair.length].urlDecode())
    }
}

@Throws(JsonSyntaxException::class)
fun String.parseJsonString(): JsonElement = JsonParser.parseString(this)

/**
 * Database Extensions
 */
fun String.toSQLiteQuery(): SimpleSQLiteQuery = SimpleSQLiteQuery(this)
