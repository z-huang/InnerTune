package com.zionhuang.innertube.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
data class Thumbnails(
    val thumbnails: List<Thumbnail>,
)

@Parcelize
@Serializable
data class Thumbnail(
    val url: String,
    val width: Int?,
    val height: Int?,
) : Parcelable {
    val isSquare: Boolean get() = width == height
}