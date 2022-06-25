package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val items: List<Item>,
    val continuation: String?,
)
