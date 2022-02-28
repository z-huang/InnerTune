package com.zionhuang.music.update

sealed class UpdateInfo {
    object NotChecked : UpdateInfo()
    object Checking : UpdateInfo()
    object UpToDate : UpdateInfo()
    object Exception : UpdateInfo()
    data class UpdateAvailable(val version: Version) : UpdateInfo()
}
