package com.zionhuang.music.extensions

import org.intellij.lang.annotations.Language
import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.find(@Language("RegExp") regex: String): RegexMatchResult? =
        regex.toMatcher(this).findNext(this)

fun String.find(@Language("RegExp") regex: String, group: String): String? = find(regex)?.groupValue(group)

fun String.find(@Language("RegExp") regex: String, vararg groups: String): List<String?>? = find(regex)?.let { m ->
    groups.map { m.groupValue(it) }
}

fun String.find(@Language("RegExp") regExs: Array<String>): RegexMatchResult? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return res
    }
    return null
}

fun String.find(@Language("RegExp") regExs: Array<String>, group: String): String? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return res.groupValue(group)
    }
    return null
}

fun String.find(@Language("RegExp") regExs: Array<String>, vararg groups: String): List<String?>? {
    for (regex in regExs) {
        val res = find(regex)
        if (res != null) return groups.map { res.groupValue(it) }
    }
    return null
}

fun String.findAll(@Language("RegExp") regex: String): Sequence<RegexMatchResult> =
        generateSequence({ find(regex) }, RegexMatchResult::next)

fun String.matchEntire(@Language("RegExp") regex: String): RegexMatchResult? =
        regex.toMatcher(this).matchEntire(this)

fun String.matchEntire(@Language("RegExp") regex: String, group: String): String? = matchEntire(regex)?.groupValue(group)

fun String.matchEntire(@Language("RegExp") regex: String, vararg groups: String): List<String?>? = matchEntire(regex)?.let { m ->
    groups.map { m.groupValue(it) }
}

fun String.matchEntire(@Language("RegExp") regExs: Array<String>): RegexMatchResult? {
    for (regex in regExs) {
        val res = matchEntire(regex)
        if (res != null) return res
    }
    return null
}

fun String.matchEntire(@Language("RegExp") regExs: Array<String>, group: String): String? {
    for (regex in regExs) {
        val res = matchEntire(regex)
        if (res != null) return res.groupValue(group)
    }
    return null
}

fun String.matchEntire(@Language("RegExp") regExs: Array<String>, vararg groups: String): List<String?>? {
    for (regex in regExs) {
        val res = matchEntire(regex)
        if (res != null) return groups.map { res.groupValue(it) }
    }
    return null
}

fun String.search(@Language("RegExp") regex: String): String? = find(regex)?.let {
    if (it.groups.size > 1) it.groupValue(1) else it.groupValue(0)
}

fun String.search(@Language("RegExp") regExs: Array<String>): String? {
    for (regex in regExs) {
        val res = search(regex)
        if (res != null) return res
    }
    return null
}

/**
 * Implementation
 */
private fun String.toMatcher(input: CharSequence) =
        Pattern.compile(this).matcher(input)

private fun Matcher.findNext(input: CharSequence, from: Int = 0): RegexMatchResult? =
        if (!find(from)) null else RegexMatchResult(this, input)

private fun Matcher.matchEntire(input: CharSequence): RegexMatchResult? =
        if (!matches()) null else RegexMatchResult(this, input)

/**
 * MatcherMatchResult
 * Duplicated from [kotlin.text.MatcherMatchResult], but add named group support
 */
class RegexMatchResult(private val matcher: Matcher, private val input: CharSequence) {
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

    fun next(): RegexMatchResult? {
        val nextIndex = matcher.end() + if (matcher.end() == matcher.start()) 1 else 0
        return if (nextIndex <= input.length) matcher.pattern().matcher(input).findNext(input, nextIndex) else null
    }
}

private fun Matcher.range(groupIndex: Int = 0): IntRange = start(groupIndex) until end(groupIndex)
private fun Matcher.range(name: String): IntRange = start(name) until end(name)