@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import kotlin.reflect.KProperty

class Preference<T : Any>(private val contextFactory: () -> Context, @StringRes private val keyId: Int, private val defaultValue: T) : SharedPreferences.OnSharedPreferenceChangeListener {
    private var isInitialized = false
    private lateinit var context: Context

    private lateinit var prefKey: String
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var value: T

    private fun init() {
        context = contextFactory()
        prefKey = context.getString(keyId)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).also {
            it.registerOnSharedPreferenceChangeListener(this)
        }
        value = (sharedPreferences.all[prefKey] as? T) ?: defaultValue
        Log.d("Preference", "init value: $value")
        isInitialized = true
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!isInitialized) init()
        Log.d("Preference", "get value: $value")
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (!isInitialized) init()
        sharedPreferences.edit().apply {
            when (value::class) {
                Boolean::class -> putBoolean(prefKey, value as Boolean)
                Float::class -> putFloat(prefKey, value as Float)
                Int::class -> putInt(prefKey, value as Int)
                Long::class -> putLong(prefKey, value as Long)
                String::class -> putString(prefKey, value as String)
            }
            apply()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == prefKey) {
            value = (sharedPreferences.all[prefKey] as? T) ?: defaultValue
        }
    }
}

fun <T : Any> Fragment.preference(@StringRes keyId: Int, defaultValue: T) = Preference({ requireContext() }, keyId, defaultValue)
fun <T : Any> AndroidViewModel.preference(@StringRes keyId: Int, defaultValue: T) = Preference({ getApplication() }, keyId, defaultValue)