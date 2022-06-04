package com.zionhuang.music.ui.activities.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.DynamicColors
import com.zionhuang.music.R
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.livedata.ThemeUtil
import com.zionhuang.music.utils.livedata.ThemeUtil.DEFAULT_THEME

abstract class ThemedBindingActivity<T : ViewBinding> : BindingActivity<T>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(sharedPreferences.getString(getString(R.string.pref_dark_theme), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())!!.toInt())
        if (DynamicColors.isDynamicColorAvailable() && sharedPreferences.getBoolean(getString(R.string.pref_follow_system_accent), true)) {
            DynamicColors.applyToActivityIfAvailable(this)
        } else {
            setTheme(ThemeUtil.getColorThemeStyleRes(sharedPreferences.getString(getString(R.string.pref_theme_color), DEFAULT_THEME)!!))
        }
        super.onCreate(savedInstanceState)
    }
}