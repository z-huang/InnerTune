package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val text: String,
    val searchEndpoint: SearchEndpoint,
)
