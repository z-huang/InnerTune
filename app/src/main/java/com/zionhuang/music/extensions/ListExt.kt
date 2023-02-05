package com.zionhuang.music.extensions

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) asReversed() else this

fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int): MutableList<T> {
    val item = removeAt(fromIndex)
    add(toIndex, item)
    return this
}
