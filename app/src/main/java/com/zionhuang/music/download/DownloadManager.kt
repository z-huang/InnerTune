package com.zionhuang.music.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.zionhuang.music.R
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.utils.SafeLiveData
import com.zionhuang.music.utils.SafeMutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DownloadManager(private val context: Context, private val scope: CoroutineScope) {
    companion object {
        private const val TAG = "YTDownloadService"
        private const val DOWNLOAD_CHANNEL_ID = "download_channel_01"
        private const val DOWNLOAD_NOTIFICATION_ID = 999
        private const val DOWNLOAD_GROUP_KEY = "com.zionhuang.music.downloadGroup"
        private const val DOWNLOAD_SUMMARY_ID = 0
    }

    private val notificationChannel = NotificationChannel(DOWNLOAD_CHANNEL_ID, context.getString(R.string.channel_name_download), NotificationManager.IMPORTANCE_DEFAULT)
    private val notificationManager = (context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager).also {
        it.createNotificationChannel(notificationChannel)
        it.notify(DOWNLOAD_SUMMARY_ID, NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_music_note_24)
                .setStyle(NotificationCompat.InboxStyle())
                .setContentTitle("DL")
                .setGroup(DOWNLOAD_GROUP_KEY)
                .setGroupSummary(true)
                .build())
    }
    private var notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)

    init {
        PRDownloader.initialize(context)
    }

    private val listeners = ArrayList<EventListener>()

    fun addEventListener(listener: EventListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }

    private val tasks = ArrayList<DownloadTask>()
    private val _tasksLiveData = SafeMutableLiveData<List<DownloadTask>>(tasks)
    val tasksLiveData: SafeLiveData<List<DownloadTask>>
        get() {
            Log.d(TAG, _tasksLiveData.value.toString())
            return _tasksLiveData
        }

    fun addDownload(task: DownloadTask) {
        tasks += task
        onTaskStarted(tasks.size - 1, task)
        PRDownloader.download(task.url, context.getExternalFilesDir(null)?.absolutePath + "/audio", task.fileName)
                .build()
                .setOnProgressListener {
                    updateState(task, it.currentBytes, it.totalBytes)
                }.start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        onDownloadCompleted(tasks.indexOf(task), task)
                    }

                    override fun onError(error: Error?) {
                        onDownloadError(tasks.indexOf(task), task, error)
                    }
                })
    }

    private val musicDatabase = MusicDatabase.getInstance(context)
    private fun onTaskStarted(index: Int, task: DownloadTask) {
        updateNotification(task.id.hashCode()) {
            setContentTitle(task.songTitle)
            setContentText("Preparing to download...")
            setProgress(0, 0, true)
        }
        _tasksLiveData.postValue(tasks)
        listeners.forEach { it.onTaskStarted(task) }
    }

    private fun onDownloadCompleted(index: Int, task: DownloadTask) {
        notificationManager.cancel(task.id.hashCode())
        tasks.remove(task)
        _tasksLiveData.postValue(tasks)
        listeners.forEach { it.onDownloadCompleted(task) }
        scope.launch {
            musicDatabase.songDao
        }
    }

    private fun onDownloadError(index: Int, task: DownloadTask, error: Error?) {
        updateNotification(task.id.hashCode()) {
            setContentTitle(task.songTitle)
            setContentText("Download failed.")
        }
        listeners.forEach { it.onDownloadError(task, error) }
    }

    private fun updateState(task: DownloadTask, currentBytes: Long, totalBytes: Long) {
        task.currentBytes = currentBytes
        task.totalBytes = totalBytes
        updateNotification(task.id.hashCode()) {
            setContentTitle(task.songTitle)
            setContentText("Downloading...")
            setProgress(totalBytes.toInt(), currentBytes.toInt(), false)
        }
        _tasksLiveData.postValue(tasks)
        listeners.forEach { it.onStateUpdated(task) }
    }

    private fun updateNotification(id: Int, applier: NotificationCompat.Builder.() -> Unit) {
        applier(notificationBuilder
                .setSmallIcon(R.drawable.ic_round_music_note_24)
                .setOngoing(true)
                .setGroup(DOWNLOAD_GROUP_KEY))
        notificationManager.notify(id, notificationBuilder.build())
    }

    interface EventListener {
        fun onTaskStarted(task: DownloadTask)
        fun onStateUpdated(task: DownloadTask)
        fun onDownloadCompleted(task: DownloadTask)
        fun onDownloadError(task: DownloadTask, error: Error?)
    }
}