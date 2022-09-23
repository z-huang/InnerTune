package com.zionhuang.innertube.utils

import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.BrowseResult
import com.zionhuang.innertube.models.YTBaseItem

suspend fun YouTube.browseAll(browseEndpoint: BrowseEndpoint): Result<List<YTBaseItem>> = runCatching {
    val items = mutableListOf<YTBaseItem>()
    var browseResult: BrowseResult? = null
    do {
        browseResult = if (browseResult == null) {
            browse(browseEndpoint).getOrThrow()
        } else {
            browse(browseResult.continuations!!).getOrThrow()
        }
        items.addAll(browseResult.items)
    } while (!browseResult?.continuations.isNullOrEmpty())
    items
}

operator fun <E : Any> E?.plus(list: List<E>): List<E> {
    if (this == null) return list
    val res = ArrayList<E>(1 + list.size)
    res.add(this)
    res.addAll(list)
    return res
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