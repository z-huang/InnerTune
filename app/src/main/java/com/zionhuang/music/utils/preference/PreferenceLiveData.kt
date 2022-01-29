package com.zionhuang.music.utils.preference

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.zionhuang.music.extensions.TAG
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.utils.livedata.SafeLiveData
import com.zionhuang.music.utils.livedata.SafeMutableLiveData

open class PreferenceLiveData<T : Any>(
    context: Context,
    @StringRes private val keyId: Int,
    private val defValue: T
) : SafeLiveData<T>(defValue) {
    protected val preferenceStore = PreferenceStore.getInstance(context)
    protected val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    protected val key = context.getString(keyId)

    private val preferenceChangeListener = OnSharedPreferenceChangeListener { _, key ->
        if (this.key == key) {
            value = preferenceStore.get(key, defValue)
        }
    }

    override fun onActive() {
        super.onActive()
        value = preferenceStore.get(key, defValue)
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }
}