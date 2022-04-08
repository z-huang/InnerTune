package com.zionhuang.music.extensions

import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.PreferenceLiveData

fun <T : Any> AndroidViewModel.preference(@StringRes keyId: Int, defaultValue: T) = Preference(getApplication(), keyId, defaultValue)
fun <T : Any> AndroidViewModel.preferenceLiveData(@StringRes keyId: Int, defaultValue: T) = PreferenceLiveData(getApplication(), keyId, defaultValue)