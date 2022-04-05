package com.zionhuang.music.extensions

import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> SharedPreferences.getSerializable(key: String): T? = getString(key, null)?.let {
    Json.decodeFromString(it) as? T
}

inline fun <reified T> SharedPreferences.putSerializable(key: String, value: T) {
    val jsonString = Json.encodeToString(value)
    edit().putString(key, jsonString).apply()
}