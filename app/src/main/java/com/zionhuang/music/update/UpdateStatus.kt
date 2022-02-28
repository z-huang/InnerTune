package com.zionhuang.music.update

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Preparing : UpdateStatus()
    data class Downloading(val downloadId: Long) : UpdateStatus()
    object Verifying : UpdateStatus()
    object Installing : UpdateStatus()
}
