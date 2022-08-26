package com.zionhuang.music.extensions

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.castOrNull(): List<T>? = if (all { it is T }) this as List<T> else null

fun <T> MutableList<T>.swap(i: Int, j: Int) {
    this[i] = this[j].also { this[j] = this[i] }
}

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) reversed() else this