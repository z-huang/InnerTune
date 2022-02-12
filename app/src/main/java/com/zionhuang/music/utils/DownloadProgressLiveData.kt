package com.zionhuang.music.utils

import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.Context
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import com.zionhuang.music.extensions.get
import com.zionhuang.music.models.DownloadProgress
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Modified by
 * https://gist.github.com/FhdAlotaibi/678eb1f4fa94475daf74ac491874fc0e
 */
class DownloadProgressLiveData(
    context: Context,
    private val downloadId: Long,
) : LiveData<DownloadProgress>(), CoroutineScope {
    private val downloadManager = context.getSystemService<DownloadManager>()!!
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    override fun onActive() {
        super.onActive()
        launch {
            while (isActive) {
                downloadManager.query(Query().setFilterById(downloadId)).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val status = cursor.get<Int>(COLUMN_STATUS)
                        postValue(DownloadProgress(status, cursor[COLUMN_BYTES_DOWNLOADED_SO_FAR], cursor[COLUMN_TOTAL_SIZE_BYTES]))
                        if (status == STATUS_SUCCESSFUL || status == STATUS_FAILED) cancel()
                    }
                }
                delay(INTERVAL)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        job.cancel()
    }

    companion object {
        const val INTERVAL: Long = 500L
    }
}