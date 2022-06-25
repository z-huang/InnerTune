package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class MusicNavigationButtonRenderer(
    val buttonText: Runs,
    val solid: Solid?,
    val icon: Icon?,
    val clickCommand: NavigationEndpoint,
) {
    fun toItem() = NavigationItem(
        title = buttonText.toString(),
        icon = icon?.iconType,
        stripeColor = solid?.leftStripeColor,
        navigationEndpoint = clickCommand
    )

    @Serializable
    data class Solid(
        val leftStripeColor: Long,
    )
}