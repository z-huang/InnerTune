package com.zionhuang.innertube.models

data class BrowseResult(
    val items: List<BaseItem>,
    val continuations: List<String>?, // act as a stack for nested continuation
) {
    fun addHeader(header: BaseItem?) = if (header == null) this else BrowseResult(
        items = listOf(header) + items,
        continuations = continuations
    )
}
