package com.zionhuang.music.utils.preference

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.zionhuang.music.utils.livedata.SafeLiveData

class PreferenceLiveData<T : Any>(
    context: Context,
    @StringRes private val keyId: Int,
    private val defValue: T
) : SafeLiveData<T>(defValue) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val key = context.getString(keyId)

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _, key ->
        if (this.key == key) {
            value = get(key, defValue)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun get(key: String?, defaultValue: T): T = when (defaultValue::class) {
        Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as Boolean)
        Float::class -> sharedPreferences.getFloat(key, defaultValue as Float)
        Int::class -> sharedPreferences.getInt(key, defaultValue as Int)
        Long::class -> sharedPreferences.getLong(key, defaultValue as Long)
        String::class -> sharedPreferences.getString(key, defaultValue as String)
        else -> throw IllegalArgumentException("Unexpected type: ${defaultValue::class.java.name}")
    } as T

    override fun onActive() {
        super.onActive()
        value = get(key, defValue)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}