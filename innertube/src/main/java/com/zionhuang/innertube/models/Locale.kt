package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Locale(
    val gl: String,
    val hl: String,
)
