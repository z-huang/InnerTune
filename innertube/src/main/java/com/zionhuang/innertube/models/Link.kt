package com.zionhuang.innertube.models

/**
 * A strict form of [Run]
 */
data class Link<T : Endpoint>(
    val text: String,
    val navigationEndpoint: T,
)
