package com.zionhuang.music.download

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope


class DownloadService : LifecycleService() {
    companion object {
        private const val TAG = "YTDownloadService"
        const val ACTION_DOWNLOAD_MUSIC = "download_music"
        const val ACTION_DOWNLOAD_ASSET  = "download_asset"
    }

    private val binder = DownloadServiceBinder()
    private lateinit var downloadManager: DownloadManager
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        downloadManager = DownloadManager(this, lifecycleScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent == null) return START_STICKY
        when (intent.action) {
            ACTION_DOWNLOAD_MUSIC -> {
                downloadManager.addMusicDownload(intent.getParcelableExtra("task")!!)
            }
            ACTION_DOWNLOAD_ASSET->{
                downloadManager.addAssetDownload(intent.getParcelableExtra("asset")!!)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    inner class DownloadServiceBinder : Binder() {
        val downloadManager: DownloadManager
            get() = this@DownloadService.downloadManager
    }
}