package com.zionhuang.music.extensions

import com.google.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils.unescapeJava
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.util.regex.Matcher
import java.util.regex.Pattern

operator fun String.get(begin: Int, end: Int) = this.substring(begin, end) // s[begin, end)

// Python-like replacement syntactic sugar
operator fun String.rem(replacement: String) = this.replaceFirst("%s", replacement)

fun String.escape(): String = Pattern.quote(this)

fun String?.unescape() = if (this != null) unescapeJava(this) else null

fun String.removeQuotes(): String {
    return when {
        this.length < 2 -> this
        this.first() == '\"' && this.last() == '\"' -> this[1, this.length - 1]
        this.first() == '\'' && this.last() == '\'' -> this[1, this.length - 1]
        else -> this
    }
}

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

@Throws(JsonSyntaxException::class)
fun String.parseJsonString(): JsonElement = JsonParser.parseString(this)

/**
 * Regex Extensions
 */

//fun MatchGroupCollection.toMatchNamedGroupCollection(): MatchNamedGroupCollection = this as MatchNamedGroupCollection
//operator fun MatchResult?.get(key: Int): String? = this?.groups?.get(key)?.value
//operator fun MatchResult?.get(key: String): String? = this?.groups?.get(key)
//operator fun MatchGroupCollection.get(key: String): String? = this.toMatchNamedGroupCollection()[key]?.value
//
//fun String.match(@Language("RegExp") regex: String): MatchResult? = regex.toRegex().matchEntire(this)
//fun String.canFind(@Language("RegExp") regex: String): Boolean = regex.toRegex().containsMatchIn(this)
//fun String.find(@Language("RegExp") regex: String): String? = regex.toRegex().find(this)?.value
//fun String.findResult(@Language("RegExp") regex: String): MatchResult? = regex.toRegex().find(this)
//fun String.findAll(@Language("RegExp") regex: String): Sequence<MatchResult> = regex.toRegex().findAll(this)
//
///* Zero or one group */
//fun String.match(@Language("RegExp") regex: String, group: String): String? = this.match(regex)[group]
//fun String.search(@Language("RegExp") regex: String): String? =
//        regex.toRegex().find(this)?.let {
//            if (it.groups.size > 1) it.groups[1]?.value else it.groups[0]?.value
//        }
//
//fun String.search(@Language("RegExp") regex: String, group: Int): String? = regex.toRegex().find(this)?.groups?.get(group)?.value
//fun String.search(@Language("RegExp") regex: String, group: String): String? = regex.toRegex().find(this)?.groups?.toMatchNamedGroupCollection()?.get(group)?.value
//
fun CharSequence.splitToJsonArray() = JsonArray().apply {
    for (c in this@splitToJsonArray) {
        add(c)
    }
}
//
//fun String.search(regExs: Array<String>): String? {
//    for (regex in regExs) {
//        val res = this.search(regex)
//        if (res != null) return res
//    }
//    return null
//}
//
//fun String.search(regExs: Array<String>, group: String): String? {
//    for (regex in regExs) {
//        val res = this.search(regex, group)
//        if (res != null) return res
//    }
//    return null
//}
//
///* Multiple groups */
//fun String.search(@Language("RegExp") regex: String, vararg groups: Int): List<String?>? = regex.toRegex().find(this)?.groups?.let {
//    groups.map { group -> it[group]?.value }
//}
//
//fun String.search(@Language("RegExp") regex: String, vararg groups: String): List<String?>? {
//    val res = regex.toRegex().find(this)?.groups as MatchNamedGroupCollection
//    return (res as MatchNamedGroupCollection).let {
//        groups.map { group ->
//            it[group]?.value
//        }
//    }
//}
//
//fun String.match(@Language("RegExp") regex: String, vararg groups: String): List<String?>? = this.match(regex)?.groups?.toMatchNamedGroupCollection()?.let {
//    groups.map { group -> it[group]?.value }
//}
//
//fun String.searchAll(@Language("RegExp") regex: String): Sequence<MatchNamedGroupCollection> = regex.toRegex().findAll(this).map { it.groups as MatchNamedGroupCollection }
//
//
//fun String.search(regExs: Array<String>, vararg groups: String): List<String?>? {
//    for (regex in regExs) {
//        val res = this.search(regex, *groups)
//        if (res != null) return res
//    }
//    return null
//}


fun String.find(regex: String): ZMatcherMatchResult? =
        regex.toMatcher(this).findNext(this)

fun String.findAll(regex: String): Sequence<ZMatcherMatchResult> =
        generateSequence({ find(regex) }, ZMatcherMatchResult::next)

fun String.matchEntire(regex: String): ZMatcherMatchResult? =
        regex.toMatcher(this).matchEntire(this)

fun String.find(regex: String, group: String): String? = find(regex)?.groupValue(group)

fun String.matchEntire(regex: String, group: String): String? = matchEntire(regex)?.groupValue(group)

fun String.find(regex: String, vararg groups: String): List<String?>? = find(regex)?.let { m ->
    groups.map { m.groupValue(it) }
}

fun String.matchEntire(regex: String, vararg groups: String): List<String?>? = matchEntire(regex)?.let { m ->
    groups.map { m.groupValue(it) }
}

fun String.find(regExs: Array<String>): ZMatcherMatchResult? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return res
    }
    return null
}

fun String.matchEntire(regExs: Array<String>): ZMatcherMatchResult? {
    for (regex in regExs) {
        val res = matchEntire(regex)
        if (res != null) return res
    }
    return null
}

fun String.find(regExs: Array<String>, group: String): String? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return res.groupValue(group)
    }
    return null
}

fun String.matchEntire(regExs: Array<String>, group: String): String? {
    for (regex in regExs) {
        val res = matchEntire(regex)
        if (res != null) return res.groupValue(group)
    }
    return null
}

fun String.find(regExs: Array<String>, vararg groups: String): List<String?>? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return groups.map { res.groupValue(it) }
    }
    return null
}

fun String.matchEntire(regExs: Array<String>, vararg groups: String): List<String?>? {
    for (regex in regExs) {
        val res = matchEntire(regex)
        if (res != null) return groups.map { res.groupValue(it) }
    }
    return null
}

fun String.search(regex: String): String? = find(regex)?.let {
    if (it.groups.size > 1) it.groupValue(1) else it.groupValue(0)
}

fun String.search(regExs: Array<String>): String? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return if (res.groups.size > 1) res.groupValue(1) else res.groupValue(0)
    }
    return null
}

private fun String.toMatcher(input: CharSequence) =
        Pattern.compile(this).matcher(input)

private fun Matcher.findNext(input: CharSequence, from: Int = 0): ZMatcherMatchResult? =
        if (!find(from)) null else ZMatcherMatchResult(this, input)

private fun Matcher.matchEntire(input: CharSequence): ZMatcherMatchResult? =
        if (!matches()) null else ZMatcherMatchResult(this, input)

/**
 * MatcherMatchResult
 * Duplicated from [kotlin.text.MatcherMatchResult], but add named group support
 */
class ZMatcherMatchResult(private val matcher: Matcher, private val input: CharSequence) {
    private val indexRange = 0 until matcher.groupCount() + 1
    val range = matcher.range()
    val value: String = matcher.group()

    val groups: MatchNamedGroupCollection = object : MatchNamedGroupCollection, AbstractCollection<MatchGroup?>() {
        override val size: Int get() = matcher.groupCount() + 1
        override fun isEmpty(): Boolean = false
        override fun iterator(): Iterator<MatchGroup?> = indices.asSequence().map { this[it] }.iterator()

        override fun get(index: Int): MatchGroup? =
                if (index in indexRange) MatchGroup(matcher.group(index)!!, matcher.range(index)) else null

        override fun get(name: String): MatchGroup? = try {
            MatchGroup(matcher.group(name)!!, matcher.range(name))
        } catch (e: Exception) {
            null
        }
    }

    fun groupValue(index: Int): String? = if (index in indexRange) matcher.group(index) else null
    fun groupValue(name: String): String? = try {
        matcher.group(name)
    } catch (e: Exception) {
        null
    }

    private var _groupValues: List<String>? = null
    val groupValues: List<String>
        get() = _groupValues ?: object : AbstractList<String>() {
            override val size: Int get() = groups.size
            override fun get(index: Int): String = matcher.group(index) ?: ""
        }.also { _groupValues = it }

    fun next(): ZMatcherMatchResult? {
        val nextIndex = matcher.end() + if (matcher.end() == matcher.start()) 1 else 0
        return if (nextIndex <= input.length) matcher.pattern().matcher(input).findNext(input, nextIndex) else null
    }
}

private fun Matcher.range(groupIndex: Int = 0): IntRange = start(groupIndex) until end(groupIndex)
private fun Matcher.range(name: String): IntRange = start(name) until end(name)
