package com.zionhuang.music.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.zionhuang.music.R

const val DOWNLOAD_MUSIC_INTENT = "download_music"

class DownloadService : Service() {
    companion object {
        private const val TAG = "YTDownloadService"
        private const val DOWNLOAD_CHANNEL_ID = "download_channel_01"
        private const val DOWNLOAD_NOTIFICATION_ID = 999
        private const val DOWNLOAD_GROUP_KEY = "com.zionhuang.music.downloadGroup"
        private const val DOWNLOAD_SUMMARY_ID = 0
    }

    private val binder = DownloadServiceBinder()
    private lateinit var notificationManager: NotificationManager
    private var notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID)

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        PRDownloader.initialize(applicationContext)
        val notificationChannel = NotificationChannel(DOWNLOAD_CHANNEL_ID, getString(R.string.channel_name_download), IMPORTANCE_DEFAULT)
        notificationManager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).also {
            it.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(DOWNLOAD_SUMMARY_ID, NotificationCompat.Builder(this, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_round_music_note_24)
                .setStyle(NotificationCompat.InboxStyle())
                .setContentTitle("DL")
                .setGroup(DOWNLOAD_GROUP_KEY)
                .setGroupSummary(true)
                .build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY
        when (intent.action) {
            DOWNLOAD_MUSIC_INTENT -> {
                Log.d(TAG, "intent action: download music, url: ${intent.extras?.getString("download_url")}")
                val id = intent.extras?.getString("id")!!
                val songTitle = intent.extras?.getString("song_title")!!
                val url = intent.extras?.getString("download_url")
                val filename = intent.extras?.getString("filename")
                updateNotification(id.hashCode()) {
                    setContentTitle(songTitle)
                    setContentText("Preparing to download...")
                    setProgress(0, 0, true)
                }
                Thread.sleep(1000)
                PRDownloader.download(url, getExternalFilesDir(null)?.absolutePath + "/audio", filename)
                        .build()
                        .setOnProgressListener {
                            updateNotification(id.hashCode()) {
                                setContentTitle(songTitle)
                                setContentText("Downloading...")
                                setProgress(it.totalBytes.toInt(), it.currentBytes.toInt(), false)
                            }
                        }.start(object : OnDownloadListener {
                            override fun onDownloadComplete() {
                                Log.d(TAG, "[$id] download complete")
                                notificationManager.cancel(id.hashCode())
                            }

                            override fun onError(error: Error?) {
                                Log.d(TAG, "[$id] download error: ${error?.serverErrorMessage}")
                            }
                        })
            }
        }
        return START_STICKY
    }

    private fun updateNotification(id: Int, applier: NotificationCompat.Builder.() -> Unit) {
        applier(notificationBuilder
                .setSmallIcon(R.drawable.ic_round_music_note_24)
                .setOngoing(true)
                .setGroup(DOWNLOAD_GROUP_KEY))
        notificationManager.notify(id, notificationBuilder.build())
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent): IBinder = binder

    class DownloadServiceBinder : Binder() {

    }
}