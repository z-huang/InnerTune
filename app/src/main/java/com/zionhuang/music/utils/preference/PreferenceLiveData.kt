package com.zionhuang.music.utils.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.zionhuang.music.extensions.get
import com.zionhuang.music.utils.livedata.SafeLiveData

open class PreferenceLiveData<T : Any>(
    context: Context,
    @StringRes private val keyId: Int,
    private val defValue: T,
) : SafeLiveData<T>(defValue) {
    protected val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    protected val key = context.getString(keyId)

    protected fun getPreferenceValue() = sharedPreferences.get(key, defValue)

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _, key ->
        if (this.key == key) {
            value = getPreferenceValue()
        }
    }

    override fun onActive() {
        super.onActive()
        value = getPreferenceValue()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}