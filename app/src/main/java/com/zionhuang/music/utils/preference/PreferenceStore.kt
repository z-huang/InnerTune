@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.utils.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * A singleton for accessing preferences with generic type, and cache used preference automatically.
 */
class PreferenceStore private constructor(context: Context) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context).apply {
        registerOnSharedPreferenceChangeListener(this@PreferenceStore)
    }

    private val cache = mutableMapOf<String, Any>()

    fun <T : Any> get(key: String, defaultValue: T): T =
        cache[key] as? T ?: (when (defaultValue::class) {
            Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as Boolean)
            Float::class -> sharedPreferences.getFloat(key, defaultValue as Float)
            Int::class -> sharedPreferences.getInt(key, defaultValue as Int)
            Long::class -> sharedPreferences.getLong(key, defaultValue as Long)
            String::class -> sharedPreferences.getString(key, defaultValue as String)
            else -> throw IllegalArgumentException("Unexpected type: ${defaultValue::class.java.name}")
        } as T).also { cache[key] = it }

    operator fun <T : Any> set(key: String, value: T) {
        sharedPreferences.edit().apply {
            when (value::class) {
                Boolean::class -> putBoolean(key, value as Boolean)
                Float::class -> putFloat(key, value as Float)
                Int::class -> putInt(key, value as Int)
                Long::class -> putLong(key, value as Long)
                String::class -> putString(key, value as String)
            }
            apply()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key in cache) {
            val old = cache[key]
                ?: throw IllegalArgumentException("Preference $key shouldn't be null.")
            cache[key] = when (old::class) {
                Boolean::class -> sharedPreferences.getBoolean(key, old as Boolean)
                Float::class -> sharedPreferences.getFloat(key, old as Float)
                Int::class -> sharedPreferences.getInt(key, old as Int)
                Long::class -> sharedPreferences.getLong(key, old as Long)
                String::class -> sharedPreferences.getString(key, old as String)
                else -> throw IllegalArgumentException("Unexpected type: ${old::class.java.name}")
            }!!
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferenceStore? = null

        @JvmStatic
        fun getInstance(context: Context): PreferenceStore {
            if (INSTANCE == null) {
                synchronized(PreferenceStore::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = PreferenceStore(context)
                    }
                }
            }
            return INSTANCE!!
        }

        private const val TAG = "PreferenceStore"
    }
}