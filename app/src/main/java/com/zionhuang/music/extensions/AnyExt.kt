package com.zionhuang.music.extensions

import android.util.Log

val Any.TAG: String
    get() = javaClass.simpleName

fun Any.logd(msg: String) {
    Log.d(TAG, msg)
}