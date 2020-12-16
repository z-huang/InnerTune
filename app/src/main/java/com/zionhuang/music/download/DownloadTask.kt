package com.zionhuang.music.download

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadTask(
        val id: String,
        val title: String? = null,
        var url: String? = null,
        var currentBytes: Long = 0,
        var totalBytes: Long = -1,
) : Parcelable {
    companion object {
        const val STATE_NOT_DOWNLOADED = 0
        const val STATE_DOWNLOADED = 1
        const val STATE_DOWNLOADING = 2
    }
}
