package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

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
    )

    @Serializable
    data class IconStyle(
        val icon: Icon,
    )
}