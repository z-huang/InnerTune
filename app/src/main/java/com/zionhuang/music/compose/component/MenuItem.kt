package com.zionhuang.music.compose.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class MenuItem(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)