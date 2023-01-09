package com.zionhuang.music.extensions

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) reversed() else this