package com.zionhuang.music.extensions

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.castOrNull(): List<T>? = if (all { it is T }) this as List<T> else null