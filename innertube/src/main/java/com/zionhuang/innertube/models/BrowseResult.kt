package com.zionhuang.innertube.models

data class BrowseResult(
    val sections: List<Section>,
    val continuation: String?,
)
