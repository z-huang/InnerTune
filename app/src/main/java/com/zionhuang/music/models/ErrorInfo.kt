package com.zionhuang.music.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ErrorInfo(
    val stackTrace: String,
) : Parcelable

fun Throwable.toErrorInfo() = ErrorInfo(
    stackTraceToString()
)