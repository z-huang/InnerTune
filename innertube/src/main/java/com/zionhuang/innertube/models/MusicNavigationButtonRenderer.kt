package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable
import java.awt.Color

@Serializable
data class MusicNavigationButtonRenderer(
    val buttonText: Runs,
    val solid: Solid?,
    val iconStyle: IconStyle?,
    val clickCommand: NavigationEndpoint,
) {
    fun toItem() = NavigationItem(
        title = buttonText.toString(),
        icon = iconStyle?.icon?.iconType,
        stripeColor = solid?.leftStripeColor,
        navigationEndpoint = clickCommand
    )

    @Serializable
    data class Solid(
        val leftStripeColor: Long,
    ) {
        fun toColor() = Color(
            ((leftStripeColor and 0xFF0000) ushr 16).toInt(),
            ((leftStripeColor and 0xFF00) ushr 8).toInt(),
            (leftStripeColor and 0xFF).toInt(),
            ((leftStripeColor and 0xFF000000) ushr 24).toInt() / 255
        )
    }

    @Serializable
    data class IconStyle(
        val icon: Icon,
    )
}