package com.zionhuang.music.utils.preference

import android.content.Context
import androidx.annotation.StringRes
import com.zionhuang.music.extensions.set

class PreferenceMutableLiveData<T : Any>(
    context: Context,
    @StringRes private val keyId: Int,
    defValue: T,
) : PreferenceLiveData<T>(context, keyId, defValue) {
    override fun postValue(value: T) {
        super.postValue(value)
        sharedPreferences[key] = value
    }

    override fun setValue(value: T) {
        super.setValue(value)
        sharedPreferences[key] = value
    }
}