package com.zionhuang.innertube.models

data class SearchResult(
    val items: List<Item>,
    val continuation: String?,
)
