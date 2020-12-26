package com.zionhuang.music.models

import android.os.Parcelable
import androidx.annotation.IntDef
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssetDownloadMission(
        @AssetType val type: Int,
        val id: String,
) : Parcelable {
    companion object {
        @IntDef(ASSET_CHANNEL)
        @Retention(AnnotationRetention.SOURCE)
        annotation class AssetType

        const val ASSET_CHANNEL = 0
    }
}
