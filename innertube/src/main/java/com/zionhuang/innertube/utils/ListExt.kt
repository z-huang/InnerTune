package com.zionhuang.innertube.utils

operator fun <E : Any> E?.plus(list: List<E>): List<E> {
    if (this == null) return list
    val res = ArrayList<E>(1 + list.size)
    res.add(this)
    res.addAll(list)
    return res
}

operator fun <E : Any> List<E>?.plus(list: List<E>?): List<E>? {
    if (this == null && list == null) return null
    val res = mutableListOf<E>()
    this?.let { res.addAll(this) }
    list?.let { res.addAll(it) }
    return res.ifEmpty { null }
}

@JvmName("plusE")
operator fun <E : Any> List<E>?.plus(list: List<E>): List<E>? {
    if (this == null) return list.ifEmpty { null }
    val res = ArrayList<E>(size + list.size)
    res.addAll(this)
    res.addAll(list)
    return res.ifEmpty { null }
}

fun <E : Any> List<E>.insertSeparator(
    generator: (before: E, after: E) -> E?,
): List<E> {
    val result = mutableListOf<E>()
    for (i in indices) {
        result.add(get(i))
        if (i != size - 1) {
            generator(get(i), get(i + 1))?.let {
                result.add(it)
            }
        }
    }
    return result
}