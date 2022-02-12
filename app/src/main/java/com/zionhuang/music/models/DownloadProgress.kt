package com.zionhuang.music.models

data class DownloadProgress(
    val status: Int,
    val currentBytes: Int = -1,
    val totalBytes: Int = -1,
)
