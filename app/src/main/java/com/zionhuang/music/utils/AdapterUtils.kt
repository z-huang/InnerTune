package com.zionhuang.music.utils

fun <T> payloadOf(oldItem: T, newItem: T): T? = if (oldItem == newItem) null else newItem