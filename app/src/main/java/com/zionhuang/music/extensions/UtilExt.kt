package com.zionhuang.music.extensions

import android.app.Application
import com.zionhuang.music.App

fun getApplication(): Application = App.INSTANCE

fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    null
}
