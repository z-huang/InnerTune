package com.zionhuang.innertube.models

data class BrowseResult(
    val items: List<YTBaseItem>,
    val lyrics: String? = null,
    val urlCanonical: String? = null,
    val continuations: List<String>? = null, // act as a stack for nested continuation
) {
    fun addHeader(header: YTBaseItem?) = if (header == null) this else copy(items = listOf(header) + items)
}
