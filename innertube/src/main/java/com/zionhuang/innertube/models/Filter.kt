package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.SearchEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val text: String,
    val searchEndpoint: SearchEndpoint,
)
