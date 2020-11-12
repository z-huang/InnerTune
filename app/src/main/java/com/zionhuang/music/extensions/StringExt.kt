package com.zionhuang.music.extensions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils.unescapeJava
import org.intellij.lang.annotations.Language
import org.jsoup.Jsoup
import java.net.URLDecoder

operator fun String.get(begin: Int, end: Int) = this.substring(begin, end) // s[begin, end)

fun String?.unescape() = if (this == null) unescapeJava(this) else null

fun String.urlDecode(): String = URLDecoder.decode(this, "UTF8")

/**
 * Remove given characters at the beginning and the end of the string.
 */
fun String.strip(chars: String) = this.replace("""^[$chars]+|[$chars]+$""".toRegex(), "")

fun String.rStrip(chars: String) = this.replace("""[$chars]+$""".toRegex(), "")

fun String.rPartition(sep: Char): Triple<String, String, String> {
    for (i in this.length - 1 downTo 0) {
        if (this[i] == sep) {
            return Triple(this.substring(0, i), sep.toString(), this.substring(i + 1, this.length))
        }
    }
    return Triple(this, "", "")
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun downloadWebPage(url: String): String = withContext(Dispatchers.IO) {
    Jsoup.connect(url).timeout(60 * 1000).get().html()
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun downloadPlainText(url: String): String = withContext(Dispatchers.IO) {
    Jsoup.connect(url).ignoreContentType(true).execute().body()
}

/**
 * JsonElement Extensions
 */

fun String.parseQueryString(): JsonObject {
    val res = JsonObject()
    val pairs = this.split("&")
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        res[pair[0, idx].urlDecode()] = JsonPrimitive(pair[idx + 1, pair.length].urlDecode())
    }
    return res
}

fun String.parseJsonString(): JsonElement = JsonParser.parseString(this)

/**
 * Regex Extensions
 */
fun String.canFind(@Language("RegExp") regex: String): Boolean = regex.toRegex().containsMatchIn(this)
fun String.find(@Language("RegExp") regex: String): String? = regex.toRegex().find(this)?.value
fun String.findAll(@Language("RegExp") regex: String): Sequence<MatchResult> = regex.toRegex().findAll(this)

/* Zero or one group */
fun String.search(@Language("RegExp") regex: String): String? =
        regex.toRegex().find(this)?.let {
            if (it.groups.size > 1) it.groups[1]?.value else it.groups[0]?.value
        }

fun String.search(@Language("RegExp") regex: String, group: Int): String? = regex.toRegex().find(this)?.groups?.get(group)?.value
fun String.search(@Language("RegExp") regex: String, group: String): String? = (regex.toRegex().find(this)?.groups as MatchNamedGroupCollection)[group]?.value

fun String.search(regExs: Array<String>): String? {
    for (regex in regExs) {
        val res = this.search(regex)
        if (res != null) return res
    }
    return null
}

fun String.search(regExs: Array<String>, group: String): String? {
    for (regex in regExs) {
        val res = this.search(regex, group)
        if (res != null) return res
    }
    return null
}

/* Multiple groups */
fun String.search(@Language("RegExp") regex: String, vararg groups: Int): List<String?>? = regex.toRegex().find(this)?.groups?.let {
    groups.map { group -> it[group]?.value }
}

fun String.search(@Language("RegExp") regex: String, vararg groups: String): List<String?>? = (regex.toRegex().find(this)?.groups as MatchNamedGroupCollection).let {
    groups.map { group -> it[group]?.value }
}

fun String.searchAll(@Language("RegExp") regex: String, vararg groups: String): Sequence<MatchNamedGroupCollection> = regex.toRegex().findAll(this).map { it.groups as MatchNamedGroupCollection }


fun String.search(regExs: Array<String>, vararg groups: String): List<String?>? {
    for (regex in regExs) {
        val res = this.search(regex, *groups)
        if (res != null) return res
    }
    return null
}