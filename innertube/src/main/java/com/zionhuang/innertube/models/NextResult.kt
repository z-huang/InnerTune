package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class NextResult(
    val items: List<SongItem>,
    val continuation: String?,
)
