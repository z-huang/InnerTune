package com.zionhuang.music.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import androidx.core.app.NotificationCompat
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.zionhuang.music.R
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADING
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import com.zionhuang.music.models.AssetDownloadMission
import com.zionhuang.music.models.AssetDownloadMission.Companion.ASSET_CHANNEL
import com.zionhuang.music.youtube.YouTubeExtractor
import com.zionhuang.music.youtube.models.YouTubeStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

typealias DownloadListener = (DownloadTask) -> Unit

class DownloadManager(private val context: Context, private val scope: CoroutineScope) {
    companion object {
        private const val TAG = "YTDownloadService"
        private const val DOWNLOAD_CHANNEL_ID = "download_channel_01"
        private const val DOWNLOAD_GROUP_KEY = "com.zionhuang.music.downloadGroup"
        private const val DOWNLOAD_SUMMARY_ID = 0
    }

    init {
        PRDownloader.initialize(context)
    }

    private val youTubeExtractor = YouTubeExtractor.getInstance(context)

    private val notificationChannel = NotificationChannel(DOWNLOAD_CHANNEL_ID, context.getString(R.string.channel_name_download), NotificationManager.IMPORTANCE_DEFAULT)
    private val notificationManager = (context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager).also {
        it.createNotificationChannel(notificationChannel)
    }
    private val summaryNotification = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_round_music_note_24)
            .setStyle(NotificationCompat.InboxStyle())
            .setGroup(DOWNLOAD_GROUP_KEY)
            .setGroupSummary(true)
            .build()

    private var notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)

    private val songRepository = SongRepository(context)

    private val listeners = ArrayList<DownloadListener>()

    fun addEventListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    private val tasks = ArrayList<DownloadTask>()

    fun addMusicDownload(task: DownloadTask) {
        tasks += task
        scope.launch {
            songRepository.updateById(task.id) {
                downloadState = STATE_DOWNLOADING
            }
            if (task.url == null) {
                when (val extractResult = youTubeExtractor.extractStream(task.id)) {
                    is YouTubeStream.Success -> {
                        val format = extractResult.formats.maxByOrNull { it.abr ?: 0 }
                                ?: return@launch songRepository.updateById(task.id) {
                                    downloadState = STATE_NOT_DOWNLOADED
                                }
                        task.url = format.url
                    }
                    is YouTubeStream.Error -> {
                        songRepository.updateById(task.id) {
                            downloadState = STATE_NOT_DOWNLOADED
                        }
                        return@launch
                    }
                }
            }

            onTaskStarted(task)
            PRDownloader.download(task.url, "${context.getExternalFilesDir(null)?.absolutePath}/audio", task.id)
                    .build()
                    .setOnProgressListener {
                        updateState(task, it.currentBytes, it.totalBytes)
                    }.start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            onDownloadCompleted(task)
                        }

                        override fun onError(error: Error?) {
                            onDownloadError(task, error)
                        }
                    })
        }
    }

    private fun onTaskStarted(task: DownloadTask) {
        updateNotification(task.id.hashCode()) {
            setContentTitle(task.title)
            setContentText("Preparing to download...")
            setProgress(0, 0, true)
        }
        listeners.forEach { it(task) }
    }

    private fun onDownloadCompleted(task: DownloadTask) {
        notificationManager.cancel(task.id.hashCode())
        tasks.remove(task)
        scope.launch {
            songRepository.updateById(task.id) {
                downloadState = STATE_DOWNLOADED
            }
        }
        listeners.forEach { it(task) }
    }

    private fun onDownloadError(task: DownloadTask, error: Error?) {
        updateNotification(task.id.hashCode()) {
            setContentTitle(task.title)
            setContentText("Download failed.")
        }
        scope.launch {
            songRepository.updateById(task.id) {
                downloadState = STATE_NOT_DOWNLOADED
            }
        }
        listeners.forEach { it(task) }
    }

    private fun updateState(task: DownloadTask, currentBytes: Long, totalBytes: Long) {
        task.currentBytes = currentBytes
        task.totalBytes = totalBytes
        updateNotification(task.id.hashCode()) {
            setContentTitle(task.title)
            setContentText("Downloading...")
            setProgress(totalBytes.toInt(), currentBytes.toInt(), false)
        }
        listeners.forEach { it(task) }
    }

    fun addAssetDownload(task:AssetDownloadMission) {
        when (task.type) {
            ASSET_CHANNEL->{

            }
        }
    }

    private fun updateNotification(id: Int, applier: NotificationCompat.Builder.() -> Unit) {
        applier(notificationBuilder
                .setSmallIcon(R.drawable.ic_round_music_note_24)
                .setOngoing(true)
                .setGroup(DOWNLOAD_GROUP_KEY))
        notificationManager.notify(id, notificationBuilder.build())
        notificationManager.notify(DOWNLOAD_SUMMARY_ID, summaryNotification)
    }
}