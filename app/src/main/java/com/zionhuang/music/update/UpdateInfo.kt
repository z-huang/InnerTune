package com.zionhuang.music.update

import kotlinx.serialization.Serializable

@Serializable
sealed class UpdateInfo {
    @Serializable
    object NotChecked : UpdateInfo()
    object Checking : UpdateInfo()
    @Serializable
    object UpToDate : UpdateInfo()
    object Exception : UpdateInfo()
    @Serializable
    data class UpdateAvailable(val version: Version) : UpdateInfo()
}
