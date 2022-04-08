package com.zionhuang.music.extensions

import android.content.Context
import android.content.SharedPreferences
import com.zionhuang.music.utils.preference.Preference
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> SharedPreferences.getSerializable(key: String, defaultValue: T): T? = getString(key, null)?.let {
    Json.decodeFromString(it) as? T
} ?: defaultValue

inline fun <reified T> SharedPreferences.putSerializable(key: String, value: T) {
    val jsonString = Json.encodeToString(value)
    edit().putString(key, jsonString).apply()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> SharedPreferences.get(key: String, defaultValue: T): T = when (defaultValue::class) {
    Boolean::class -> getBoolean(key, defaultValue as Boolean)
    Float::class -> getFloat(key, defaultValue as Float)
    Int::class -> getInt(key, defaultValue as Int)
    Long::class -> getLong(key, defaultValue as Long)
    String::class -> getString(key, defaultValue as String)
    else -> throw IllegalArgumentException("Unexpected type: ${defaultValue::class.java.name}")
} as T

operator fun <T : Any> SharedPreferences.set(key: String, value: T) {
    edit().apply {
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
