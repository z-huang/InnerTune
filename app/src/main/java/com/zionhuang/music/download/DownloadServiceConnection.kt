package com.zionhuang.music.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class DownloadServiceConnection(context: Context) {
    private val connection = DownloadServiceConnection()
    private var downloadManager: DownloadManager? = null

    private var listeners = mutableListOf<DownloadListener>()
    private val listener: DownloadListener = { task -> listeners.forEach { it(task) } }

    fun addDownloadListener(listener: DownloadListener) {
        listeners.add(listener)
    }

    fun removeDownloadListener(listener: DownloadListener) {
        listeners.remove(listener)
    }

    inner class DownloadServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            downloadManager = (service as? DownloadService.DownloadServiceBinder)?.downloadManager
            downloadManager?.addEventListener(listener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            downloadManager?.removeListener(listener)
        }
    }

    init {
        val intent = Intent(context, DownloadService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
}