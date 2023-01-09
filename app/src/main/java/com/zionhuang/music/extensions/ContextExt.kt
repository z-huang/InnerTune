package com.zionhuang.music.extensions

import android.content.Context
import android.content.SharedPreferences
import com.zionhuang.music.utils.Preference

val Context.sharedPreferences: SharedPreferences
    get() = getSharedPreferences("preferences", Context.MODE_PRIVATE)

fun <T : Any> Context.preference(key: String, defaultValue: T) = Preference(this, key, defaultValue)
