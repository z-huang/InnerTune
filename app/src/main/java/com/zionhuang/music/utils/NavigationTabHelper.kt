package com.zionhuang.music.utils

import android.content.Context
import androidx.core.content.edit
import com.zionhuang.music.constants.NAV_TAB_CONFIG
import com.zionhuang.music.extensions.sharedPreferences

object NavigationTabHelper {
    fun getConfig(context: Context): BooleanArray = try {
        context.sharedPreferences.getString(NAV_TAB_CONFIG, null)!!
            .split(",")
            .map { it == "true" }
            .toBooleanArray()
    } catch (e: Exception) {
        BooleanArray(5) { true }
    }

    fun setConfig(context: Context, enabledItems: BooleanArray) = context.sharedPreferences.edit {
        putString(NAV_TAB_CONFIG, enabledItems.joinToString(","))
    }
}
