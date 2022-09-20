package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicDescriptionShelfRenderer(
    val header: Runs?,
    val subheader: Runs?,
    val description: Runs,
    val footer: Runs?,
) {
    fun toSectionHeader() = header?.let {
        Header(
            title = it.toString(),
            subtitle = subheader?.toString()
        )
    }
}
