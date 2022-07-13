package com.zionhuang.innertube.models

data class BrowseResult(
    val items: List<BaseItem>,
    val continuation: String?,
) {
    fun addHeader(header: BaseItem?) = if (header == null) this else BrowseResult(
        items = listOf(header) + items,
        continuation = continuation
    )
}
