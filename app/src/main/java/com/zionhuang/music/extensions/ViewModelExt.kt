package com.zionhuang.music.extensions

import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.zionhuang.music.utils.preference.Preference

fun <T : Any> AndroidViewModel.preference(@StringRes keyId: Int, defaultValue: T) = Preference({ getApplication() }, keyId, defaultValue)