@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.utils.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.zionhuang.music.extensions.*
import kotlin.reflect.KProperty

open class Preference<T : Any>(
    context: Context,
    @StringRes private val keyId: Int,
    private val defaultValue: T,
) {
    protected var key: String = context.getString(keyId)
    protected var sharedPreferences: SharedPreferences = context.sharedPreferences

    protected open fun getPreferenceValue(): T = sharedPreferences.get(key, defaultValue)
    protected open fun setPreferenceValue(value: T) {
        sharedPreferences[key] = value
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = getPreferenceValue()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = setPreferenceValue(value)
}

inline fun <reified E : Enum<E>> enumPreference(context: Context, keyId: Int, defaultValue: E): Preference<E> = object : Preference<E>(context, keyId, defaultValue) {
    override fun getPreferenceValue(): E = sharedPreferences.getEnum(key, defaultValue)
    override fun setPreferenceValue(value: E) {
        sharedPreferences.edit { putEnum(key, value) }
    }
}