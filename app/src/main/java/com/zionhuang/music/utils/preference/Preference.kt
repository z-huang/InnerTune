@file:Suppress("UNCHECKED_CAST")

package com.zionhuang.music.utils.preference

import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import kotlin.reflect.KProperty

/**
 * A delegate providing easy access to dynamic preference value through [PreferenceStore]
 */
class Preference<T : Any>(
        private val contextFactory: () -> Context,
        @StringRes private val keyId: Int,
        private val defaultValue: T,
) {
    private var isInitialized = false

    private lateinit var key: String
    private lateinit var preferenceStore: PreferenceStore

    private fun init() {
        contextFactory().let { context ->
            key = context.getString(keyId)
            preferenceStore = PreferenceStore.getInstance(context)
        }
        isInitialized = true
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!isInitialized) init()
        return preferenceStore.get(key, defaultValue).also {
            Log.d("Preference", "get value: $it")
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (!isInitialized) init()
        preferenceStore[key] = value
    }
}
