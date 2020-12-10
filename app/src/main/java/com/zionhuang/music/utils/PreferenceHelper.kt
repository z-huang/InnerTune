package com.zionhuang.music.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager

@Suppress("UNCHECKED_CAST")
class PreferenceHelper<T : Any>(context: Context, @StringRes keyId: Int, private val defaultValue: T) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefKey: String = context.getString(keyId)
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).also {
        it.registerOnSharedPreferenceChangeListener(this)
    }

    private var _value: Any? = sharedPreferences.all[prefKey]
    val value: T
        get() = if (_value != null) _value as T else defaultValue

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == prefKey) {
            _value = sharedPreferences.all[key]
        }
    }
}