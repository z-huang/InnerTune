package com.zionhuang.music.ui.activities.base

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.DynamicColors
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.livedata.ThemeUtil
import com.zionhuang.music.utils.livedata.ThemeUtil.DEFAULT_THEME

abstract class ThemedBindingActivity<T : ViewBinding> : BindingActivity<T>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (DynamicColors.isDynamicColorAvailable() && sharedPreferences.getBoolean(getString(R.string.pref_follow_system_accent), true)) {
            DynamicColors.applyToActivityIfAvailable(this)
        } else {
            val colorTheme: String by preference(R.string.pref_theme_color, DEFAULT_THEME)
            val darkTheme: String by preference(R.string.pref_dark_theme, getString(R.string.mode_night_follow_system))
            setTheme(ThemeUtil.getColorThemeStyleRes(colorTheme, darkTheme))
        }
        super.onCreate(savedInstanceState)
    }
}