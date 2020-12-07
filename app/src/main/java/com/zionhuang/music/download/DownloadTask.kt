package com.zionhuang.music.download

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class DownloadStatus {
    PENDING,
    RUNNING,
    FINISHED,
    ERROR
}

@Parcelize
data class DownloadTask(
        val id: String,
        val songTitle: String,
        val url: String,
        val fileName: String,
        var currentBytes: Long = 0,
        var totalBytes: Long = -1,
        val status: DownloadStatus = DownloadStatus.PENDING,
) : Parcelable
