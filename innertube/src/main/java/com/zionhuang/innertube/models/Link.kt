package com.zionhuang.innertube.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A strict form of [Run]
 */
@Parcelize
data class Link<T : Endpoint>(
    val text: String,
    val navigationEndpoint: T,
) : Parcelable