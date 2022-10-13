package com.zionhuang.music.utils

import android.content.Context
import android.util.Log
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.extensions.sharedPreferences

class NavBarHelper(
    private val context: Context
) {
    val pref by context.preference(R.string.visible_tabs, "")

    fun getEnabledItems(): BooleanArray {
        try {
            val items = pref.split(",").map {
                it == "true"
            }.toBooleanArray()
            if (items.size == navBarItemsSize) return items
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return BooleanArray(navBarItemsSize) { true }
    }

    fun setEnabledItems(enabledItems: BooleanArray) {
        Log.e("tabs", enabledItems.joinToString(","))
        context.sharedPreferences.edit().putString(
            context.getString(R.string.visible_tabs),
            enabledItems.joinToString(",")
        ).apply()
    }

    companion object {
        const val navBarItemsSize = 5
    }
}
