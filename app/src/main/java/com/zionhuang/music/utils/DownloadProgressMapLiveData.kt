package com.zionhuang.music.utils

import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.Context
import androidx.core.content.getSystemService
import androidx.lifecycle.LiveData
import com.zionhuang.music.db.entities.DownloadEntity
import com.zionhuang.music.extensions.forEach
import com.zionhuang.music.extensions.get
import com.zionhuang.music.models.DownloadProgress
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Modified by
 * https://gist.github.com/FhdAlotaibi/678eb1f4fa94475daf74ac491874fc0e
 */
class DownloadProgressMapLiveData(
    context: Context,
    downloadEntities: List<DownloadEntity>,
) : LiveData<Map<String, DownloadProgress>>(emptyMap()), CoroutineScope {
    private val dlIdToSongId = downloadEntities.associate { it.id to it.songId }
    private val dlIds = downloadEntities.map { it.id }.toMutableList()
    private val downloadManager = context.getSystemService<DownloadManager>()!!
    private val job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    override fun onActive() {
        super.onActive()
        if (dlIds.isEmpty()) return
        launch {
            while (isActive) {
                val result = mutableMapOf<String, DownloadProgress>()
                downloadManager.query(Query().setFilterById(*dlIds.toLongArray())).forEach {
                    val id = get<Long>(COLUMN_ID)
                    val status = get<Int>(COLUMN_STATUS)
                    result[dlIdToSongId[id]!!] = DownloadProgress(
                        status = get(COLUMN_STATUS),
                        currentBytes = get(COLUMN_BYTES_DOWNLOADED_SO_FAR),
                        totalBytes = get(COLUMN_TOTAL_SIZE_BYTES)
                    )
                    if (status == STATUS_SUCCESSFUL || status == STATUS_FAILED) dlIds.remove(id)
                }
                postValue(result)
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