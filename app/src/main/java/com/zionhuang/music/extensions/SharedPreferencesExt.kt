package com.zionhuang.music.extensions

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

inline fun <reified E : Enum<E>> SharedPreferences.getEnum(key: String, defaultValue: E): E = getString(key, null)?.let {
    try {
        enumValueOf<E>(it)
    } catch (e: IllegalArgumentException) {
        null
    }
} ?: defaultValue

inline fun <reified T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T) = putString(key, value.name)

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
    edit {
        when (value::class) {
            Boolean::class -> putBoolean(key, value as Boolean)
            Float::class -> putFloat(key, value as Float)
            Int::class -> putInt(key, value as Int)
            Long::class -> putLong(key, value as Long)
            String::class -> putString(key, value as String)
        }
    }
}

val SharedPreferences.keyFlow: Flow<String?>
    get() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
            trySend(key)
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

fun SharedPreferences.booleanFlow(key: String, defaultValue: Boolean) = keyFlow
    .filter { it == key || it == null }
    .onStart { emit("init trigger") }
    .map { getBoolean(key, defaultValue) }
    .conflate()

@Composable
fun preferenceState(key: String, defaultValue: Boolean) =
    LocalContext.current.sharedPreferences.booleanFlow(key, defaultValue).collectAsState(
        initial = LocalContext.current.sharedPreferences.getBoolean(key, defaultValue)
    )

inline fun <reified E : Enum<E>> SharedPreferences.enumFlow(key: String, defaultValue: E) = keyFlow
    .filter { it == key || it == null }
    .onStart { emit("init trigger") }
    .map { getEnum(key, defaultValue) }
    .conflate()
