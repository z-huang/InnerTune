package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.downloader.Error
import com.zionhuang.music.download.DownloadManager
import com.zionhuang.music.download.DownloadService
import com.zionhuang.music.download.DownloadTask

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val connection = DownloadServiceConnection()
    private var downloadManager: DownloadManager? = null

    init {
        val intent = Intent(application, DownloadService::class.java)
        application.startService(intent)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private var _tasks = MutableLiveData<List<DownloadTask>>(emptyList())
    val tasks: LiveData<List<DownloadTask>>
        get() = _tasks

    private var listener: DownloadManager.EventListener? = null

    fun setDownloadListener(listener: DownloadManager.EventListener) {
        this.listener = listener
        downloadManager?.addEventListener(listener)
    }

    private val observer = Observer<List<DownloadTask>> {
        _tasks.postValue(it)
    }

    inner class DownloadServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            downloadManager = (service as? DownloadService.DownloadServiceBinder)?.downloadManager
            listener?.let {
                downloadManager?.addEventListener(it)
            }
            downloadManager?.tasksLiveData?.observeForever(observer)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            listener?.let {
                downloadManager?.removeListener(it)
            }
            downloadManager?.tasksLiveData?.removeObserver(observer)
        }

    }
}