package com.zionhuang.music.utils

import android.content.Context
import androidx.core.content.edit
import com.zionhuang.music.R
import com.zionhuang.music.extensions.sharedPreferences

object NavigationTabHelper {
    fun getConfig(context: Context): BooleanArray = try {
        context.sharedPreferences.getString(context.getString(R.string.pref_nav_tab_config), null)!!
            .split(",")
            .map { it == "true" }
            .toBooleanArray()
    } catch (e: Exception) {
        BooleanArray(context.resources.getStringArray(R.array.bottom_nav_items).size) { true }
    }

    fun setConfig(context: Context, enabledItems: BooleanArray) = context.sharedPreferences.edit {
        putString(context.getString(R.string.pref_nav_tab_config), enabledItems.joinToString(","))
    }
}
