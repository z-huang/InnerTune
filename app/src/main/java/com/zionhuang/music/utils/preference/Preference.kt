@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.utils.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.zionhuang.music.extensions.get
import com.zionhuang.music.extensions.set
import kotlin.reflect.KProperty

open class Preference<T : Any>(
    private val contextFactory: () -> Context,
    @StringRes private val keyId: Int,
    private val defaultValue: T,
) {
    private var isInitialized = false

    protected lateinit var key: String
    protected lateinit var sharedPreferences: SharedPreferences

    private fun init() {
        contextFactory().let { context ->
            key = context.getString(keyId)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
        isInitialized = true
    }

    protected open fun getPreferenceValue(): T = sharedPreferences.get(key, defaultValue)
    protected open fun setPreferenceValue(value: T) {
        sharedPreferences[key] = value
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!isInitialized) init()
        return getPreferenceValue()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (!isInitialized) init()
        setPreferenceValue(value)
    }
}
