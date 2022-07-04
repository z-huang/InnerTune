package com.zionhuang.innertube.models

data class BrowseResult(
    val sections: List<Section>,
    val continuation: String?,
) {
    fun addHeader(section: Section?) = if (section == null) this else BrowseResult(
        sections = listOf(section) + sections,
        continuation = continuation
    )
}
