package com.zionhuang.innertube.models

data class NextResult(
    val items: List<Item>,
    val currentIndex: Int? = null,
    val continuation: String?,
)
