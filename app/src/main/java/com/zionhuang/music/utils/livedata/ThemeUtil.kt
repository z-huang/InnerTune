package com.zionhuang.music.utils.livedata

import androidx.annotation.StyleRes
import com.zionhuang.music.R

object ThemeUtil {
    private val colorThemeMap: Map<String, Int> = mapOf(
        "SAKURA" to R.style.ThemeOverlay_MaterialSakura,
        "MATERIAL_RED" to R.style.ThemeOverlay_MaterialRed,
        "MATERIAL_PINK" to R.style.ThemeOverlay_MaterialPink,
        "MATERIAL_PURPLE" to R.style.ThemeOverlay_MaterialPurple,
        "MATERIAL_DEEP_PURPLE" to R.style.ThemeOverlay_MaterialDeepPurple,
        "MATERIAL_INDIGO" to R.style.ThemeOverlay_MaterialIndigo,
        "MATERIAL_BLUE" to R.style.ThemeOverlay_MaterialBlue,
        "MATERIAL_LIGHT_BLUE" to R.style.ThemeOverlay_MaterialLightBlue,
        "MATERIAL_CYAN" to R.style.ThemeOverlay_MaterialCyan,
        "MATERIAL_TEAL" to R.style.ThemeOverlay_MaterialTeal,
        "MATERIAL_GREEN" to R.style.ThemeOverlay_MaterialGreen,
        "MATERIAL_LIGHT_GREEN" to R.style.ThemeOverlay_MaterialLightGreen,
        "MATERIAL_LIME" to R.style.ThemeOverlay_MaterialLime,
        "MATERIAL_YELLOW" to R.style.ThemeOverlay_MaterialYellow,
        "MATERIAL_AMBER" to R.style.ThemeOverlay_MaterialAmber,
        "MATERIAL_ORANGE" to R.style.ThemeOverlay_MaterialOrange,
        "MATERIAL_DEEP_ORANGE" to R.style.ThemeOverlay_MaterialDeepOrange,
        "MATERIAL_BROWN" to R.style.ThemeOverlay_MaterialBrown,
        "MATERIAL_BLUE_GREY" to R.style.ThemeOverlay_MaterialBlueGrey
    )

    private val lightColorThemeMap: Map<String, Int> = mapOf(
        "SAKURA" to R.style.ThemeOverlay_Light_MaterialSakura,
        "MATERIAL_RED" to R.style.ThemeOverlay_Light_MaterialRed,
        "MATERIAL_PINK" to R.style.ThemeOverlay_Light_MaterialPink,
        "MATERIAL_PURPLE" to R.style.ThemeOverlay_Light_MaterialPurple,
        "MATERIAL_DEEP_PURPLE" to R.style.ThemeOverlay_Light_MaterialDeepPurple,
        "MATERIAL_INDIGO" to R.style.ThemeOverlay_Light_MaterialIndigo,
        "MATERIAL_BLUE" to R.style.ThemeOverlay_Light_MaterialBlue,
        "MATERIAL_LIGHT_BLUE" to R.style.ThemeOverlay_Light_MaterialLightBlue,
        "MATERIAL_CYAN" to R.style.ThemeOverlay_Light_MaterialCyan,
        "MATERIAL_TEAL" to R.style.ThemeOverlay_Light_MaterialTeal,
        "MATERIAL_GREEN" to R.style.ThemeOverlay_Light_MaterialGreen,
        "MATERIAL_LIGHT_GREEN" to R.style.ThemeOverlay_Light_MaterialLightGreen,
        "MATERIAL_LIME" to R.style.ThemeOverlay_Light_MaterialLime,
        "MATERIAL_YELLOW" to R.style.ThemeOverlay_Light_MaterialYellow,
        "MATERIAL_AMBER" to R.style.ThemeOverlay_Light_MaterialAmber,
        "MATERIAL_ORANGE" to R.style.ThemeOverlay_Light_MaterialOrange,
        "MATERIAL_DEEP_ORANGE" to R.style.ThemeOverlay_Light_MaterialDeepOrange,
        "MATERIAL_BROWN" to R.style.ThemeOverlay_Light_MaterialBrown,
        "MATERIAL_BLUE_GREY" to R.style.ThemeOverlay_Light_MaterialBlueGrey
    )

    private val darkColorThemeMap: Map<String, Int> = mapOf(
        "SAKURA" to R.style.ThemeOverlay_Dark_MaterialSakura,
        "MATERIAL_RED" to R.style.ThemeOverlay_Dark_MaterialRed,
        "MATERIAL_PINK" to R.style.ThemeOverlay_Dark_MaterialPink,
        "MATERIAL_PURPLE" to R.style.ThemeOverlay_Dark_MaterialPurple,
        "MATERIAL_DEEP_PURPLE" to R.style.ThemeOverlay_Dark_MaterialDeepPurple,
        "MATERIAL_INDIGO" to R.style.ThemeOverlay_Dark_MaterialIndigo,
        "MATERIAL_BLUE" to R.style.ThemeOverlay_Dark_MaterialBlue,
        "MATERIAL_LIGHT_BLUE" to R.style.ThemeOverlay_Dark_MaterialLightBlue,
        "MATERIAL_CYAN" to R.style.ThemeOverlay_Dark_MaterialCyan,
        "MATERIAL_TEAL" to R.style.ThemeOverlay_Dark_MaterialTeal,
        "MATERIAL_GREEN" to R.style.ThemeOverlay_Dark_MaterialGreen,
        "MATERIAL_LIGHT_GREEN" to R.style.ThemeOverlay_Dark_MaterialLightGreen,
        "MATERIAL_LIME" to R.style.ThemeOverlay_Dark_MaterialLime,
        "MATERIAL_YELLOW" to R.style.ThemeOverlay_Dark_MaterialYellow,
        "MATERIAL_AMBER" to R.style.ThemeOverlay_Dark_MaterialAmber,
        "MATERIAL_ORANGE" to R.style.ThemeOverlay_Dark_MaterialOrange,
        "MATERIAL_DEEP_ORANGE" to R.style.ThemeOverlay_Dark_MaterialDeepOrange,
        "MATERIAL_BROWN" to R.style.ThemeOverlay_Dark_MaterialBrown,
        "MATERIAL_BLUE_GREY" to R.style.ThemeOverlay_Dark_MaterialBlueGrey
    )

    const val DEFAULT_THEME = "MATERIAL_BLUE"
    const val MODE_NIGHT_NO = "MODE_NIGHT_NO"
    const val MODE_NIGHT_YES = "MODE_NIGHT_YES"
    const val MODE_NIGHT_FOLLOW_SYSTEM = "MODE_NIGHT_FOLLOW_SYSTEM"


    @StyleRes
    fun getColorThemeStyleRes(theme: String, darkTheme: String, defaultTheme: String = DEFAULT_THEME): Int {
        val map = when (darkTheme) {
            MODE_NIGHT_NO -> lightColorThemeMap
            MODE_NIGHT_YES -> darkColorThemeMap
            MODE_NIGHT_FOLLOW_SYSTEM -> colorThemeMap
            else -> throw IllegalArgumentException()
        }
        return map[theme] ?: map[defaultTheme]!!
    }
}
