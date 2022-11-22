package com.zionhuang.music.compose.utils

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.zionhuang.music.extensions.getEnum
import com.zionhuang.music.extensions.putEnum
import com.zionhuang.music.extensions.sharedPreferences


@Composable
fun rememberPreference(key: String, defaultValue: Boolean): MutableState<Boolean> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.sharedPreferences.getBoolean(key, defaultValue)) {
            context.sharedPreferences.edit { putBoolean(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: Int): MutableState<Int> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.sharedPreferences.getInt(key, defaultValue)) {
            context.sharedPreferences.edit { putInt(key, it) }
        }
    }
}

@Composable
fun rememberPreference(key: String, defaultValue: String): MutableState<String> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.sharedPreferences.getString(key, null) ?: defaultValue) {
            context.sharedPreferences.edit { putString(key, it) }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> rememberPreference(key: String, defaultValue: T): MutableState<T> {
    val context = LocalContext.current
    return remember {
        mutableStatePreferenceOf(context.sharedPreferences.getEnum(key, defaultValue)) {
            context.sharedPreferences.edit { putEnum(key, it) }
        }
    }
}

inline fun <T> mutableStatePreferenceOf(
    value: T,
    crossinline onStructuralInequality: (newValue: T) -> Unit,
) = mutableStateOf(
    value = value,
    policy = object : SnapshotMutationPolicy<T> {
        override fun equivalent(a: T, b: T): Boolean {
            val areEquals = a == b
            if (!areEquals) onStructuralInequality(b)
            return areEquals
        }
    })
