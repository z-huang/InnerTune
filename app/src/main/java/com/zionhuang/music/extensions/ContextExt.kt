package com.zionhuang.music.extensions

import android.content.Context

fun Context.getDensity(): Float = resources.displayMetrics.density
