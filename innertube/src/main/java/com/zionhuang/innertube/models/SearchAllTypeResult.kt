package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchAllTypeResult(
    val filters: List<Filter>?,
    val sections: List<Section>,
)
