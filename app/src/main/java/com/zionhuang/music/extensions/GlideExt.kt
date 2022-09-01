package com.zionhuang.music.extensions

import com.zionhuang.music.utils.GlideRequest
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking

fun <R> GlideRequest<R>.getBlocking(): R? =
        try {
            runBlocking(IO) {
                submit().get()
            }
        } catch (e: Exception) {
            null
        }
