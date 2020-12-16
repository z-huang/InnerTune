package com.zionhuang.music.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.getDensity(): Float = resources.displayMetrics.density

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}