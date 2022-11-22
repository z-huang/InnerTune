package com.zionhuang.music.utils

import androidx.annotation.StyleRes

object ThemeUtil {
    private val colorThemeMap: Map<String, Int> = mapOf()

    const val DEFAULT_THEME = "MATERIAL_BLUE"

    @StyleRes
    fun getColorThemeStyleRes(theme: String) = colorThemeMap[theme]!!
}
