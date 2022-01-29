package com.zionhuang.music.utils.preference

import android.content.Context
import androidx.annotation.StringRes

class PreferenceMutableLiveData<T : Any>(
    context: Context,
    @StringRes private val keyId: Int,
    defValue: T,
) : PreferenceLiveData<T>(context, keyId, defValue) {
    override fun postValue(value: T) {
        super.postValue(value)
        preferenceStore[key] = value
    }

    override fun setValue(value: T) {
        super.setValue(value)
        preferenceStore[key] = value
    }
}