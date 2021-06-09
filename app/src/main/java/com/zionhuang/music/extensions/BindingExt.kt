package com.zionhuang.music.extensions

import android.content.Context
import androidx.viewbinding.ViewBinding

val ViewBinding.context: Context
    get() = root.context