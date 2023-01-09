@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.zionhuang.music.extensions.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Preference<T : Any>(
    context: Context,
    private val key: String,
    private val defaultValue: T,
) : ReadWriteProperty<Any?, T> {
    protected var sharedPreferences: SharedPreferences = context.sharedPreferences

    protected open fun getPreferenceValue(): T = sharedPreferences.get(key, defaultValue)
    protected open fun setPreferenceValue(value: T) {
        sharedPreferences[key] = value
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getPreferenceValue()
    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setPreferenceValue(value)
}

inline fun <reified E : Enum<E>> enumPreference(context: Context, key: String, defaultValue: E): Preference<E> = object : Preference<E>(context, key, defaultValue) {
    override fun getPreferenceValue(): E = sharedPreferences.getEnum(key, defaultValue)
    override fun setPreferenceValue(value: E) {
        sharedPreferences.edit { putEnum(key, value) }
    }
}