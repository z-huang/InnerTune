package com.zionhuang.innertube.models

data class NextResult(
    val items: List<SongItem>,
    val currentIndex: Int? = null,
    val continuation: String?,
)
