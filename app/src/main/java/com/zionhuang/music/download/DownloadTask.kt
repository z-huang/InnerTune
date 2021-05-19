package com.zionhuang.music.download

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DownloadTask(
    val id: String,
    var downloadId: Int = -1,
    var title: String? = null,
    var currentBytes: Long = 0,
    var totalBytes: Long = -1,
    var state: Int = 0,
    var error: Error? = null,
) : Parcelable {
    companion object {
        const val STATE_NOT_DOWNLOADED = 0
        const val STATE_DOWNLOADED = 1
        const val STATE_DOWNLOADING = 2
    }
}
