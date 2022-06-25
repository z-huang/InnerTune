package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class BrowseResult(
    val sections: List<Section>,
    val continuation: String?,
)
