package com.zionhuang.music.models

data class DownloadProgress(
    val status: Int,
    val currentBytes: Int,
    val totalBytes: Int,
)
