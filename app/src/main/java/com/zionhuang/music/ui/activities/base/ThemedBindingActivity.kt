package com.zionhuang.music.ui.activities.base

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.DynamicColors
import com.zionhuang.music.R
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.livedata.ThemeUtil
import com.zionhuang.music.utils.livedata.ThemeUtil.DEFAULT_THEME

abstract class ThemedBindingActivity<T : ViewBinding> : BindingActivity<T>(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Fix preference type mismatch in 0.3.0
        try {
            sharedPreferences.getString(getString(R.string.pref_dark_theme), "MODE_FOLLOW_SYSTEM")!!.toInt()
        } catch (e: Exception) {
            sharedPreferences.edit()
                .putString(getString(R.string.pref_dark_theme), MODE_NIGHT_FOLLOW_SYSTEM.toString())
                .commit()
        }

        AppCompatDelegate.setDefaultNightMode(sharedPreferences.getString(getString(R.string.pref_dark_theme), MODE_NIGHT_FOLLOW_SYSTEM.toString())!!.toInt())
        if (DynamicColors.isDynamicColorAvailable() && sharedPreferences.getBoolean(getString(R.string.pref_follow_system_accent), true)) {
            DynamicColors.applyToActivityIfAvailable(this)
        } else {
            setTheme(ThemeUtil.getColorThemeStyleRes(sharedPreferences.getString(getString(R.string.pref_theme_color), DEFAULT_THEME)!!))
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        super.onCreate(savedInstanceState)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key in listOf(
                getString(R.string.pref_dark_theme),
                getString(R.string.pref_follow_system_accent),
                getString(R.string.pref_theme_color))
        ) {
            recreate()
        }
    }
}