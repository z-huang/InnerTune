package com.zionhuang.music.extensions

import android.content.SharedPreferences
import androidx.compose.runtime.*
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


val LocalSharedPreferences = staticCompositionLocalOf<SharedPreferences> { error("SharedPreferences not provided") }
val LocalSharedPreferencesKeyFlow = staticCompositionLocalOf<Flow<String?>> { error("SharedPreferences key flow not provided") }

class PerferenceMutableState<T>(
    val state: State<T>,
    val onChange: (T) -> Unit,
) : MutableState<T> {
    override var value: T = state.value
    override fun component1(): T = state.value
    override fun component2(): (T) -> Unit = onChange
}

@Composable
fun mutablePreferenceState(key: String, defaultValue: Boolean): PerferenceMutableState<Boolean> {
    val sharedPreferences = LocalSharedPreferences.current
    val keyFlow = LocalSharedPreferencesKeyFlow.current
    return PerferenceMutableState(
        state = produceState(initialValue = LocalSharedPreferences.current.getBoolean(key, defaultValue)) {
            keyFlow.filter { it == null || it == key }.collect {
                value = sharedPreferences.getBoolean(key, defaultValue)
            }
        },
        onChange = { value ->
            sharedPreferences.edit {
                putBoolean(key, value)
            }
        }
    )
}

@Composable
fun mutablePreferenceState(key: String, defaultValue: String): PerferenceMutableState<String> {
    val sharedPreferences = LocalSharedPreferences.current
    val keyFlow = LocalSharedPreferencesKeyFlow.current
    return PerferenceMutableState(
        state = produceState(initialValue = LocalSharedPreferences.current.getString(key, defaultValue)!!) {
            keyFlow.filter { it == null || it == key }.collect {
                value = sharedPreferences.getString(key, defaultValue)!!
            }
        },
        onChange = { value ->
            sharedPreferences.edit {
                putString(key, value)
            }
        }
    )
}

@Composable
inline fun <reified T : Enum<T>> mutablePreferenceState(key: String, defaultValue: T): PerferenceMutableState<T> {
    val sharedPreferences = LocalSharedPreferences.current
    val keyFlow = LocalSharedPreferencesKeyFlow.current
    return PerferenceMutableState(
        state = produceState(initialValue = LocalSharedPreferences.current.getEnum(key, defaultValue)) {
            keyFlow.filter { it == null || it == key }.collect {
                value = sharedPreferences.getEnum(key, defaultValue)
            }
        },
        onChange = { value ->
            sharedPreferences.edit {
                putEnum(key, value)
            }
        }
    )
}
