package com.zionhuang.music.utils

import android.util.Log
import com.zionhuang.music.BuildConfig
import kotlin.system.measureTimeMillis

fun <T> logTimeMillis(tag: String, msg: String, block: () -> T): T {
    if (!BuildConfig.DEBUG) return block()
    var result: T
    val duration = measureTimeMillis {
        result = block()
    }
    Log.d(tag, msg.format(duration))
    return result
}