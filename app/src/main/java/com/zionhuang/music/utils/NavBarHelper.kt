package com.zionhuang.music.utils

import android.content.Context
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.extensions.sharedPreferences

class NavBarHelper(
    private val context: Context
) {
    val pref by context.preference(R.string.visible_tabs, "")

    fun getEnabledItems(): BooleanArray {
        return try {
            pref.split(",").map {
                it == "true"
            }.toBooleanArray()
        } catch (e: Exception) {
            BooleanArray(5) { true }
        }
    }

    fun setEnabledItems(enabledItems: BooleanArray) {
        context.sharedPreferences.edit().putString(
            context.getString(R.string.visible_tabs),
            enabledItems.joinToString("")
        ).apply()
    }
}
