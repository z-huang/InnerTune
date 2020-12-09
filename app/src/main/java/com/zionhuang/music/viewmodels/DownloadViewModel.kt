package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.download.DownloadListener
import com.zionhuang.music.download.DownloadManager
import com.zionhuang.music.download.DownloadService
import kotlinx.coroutines.flow.Flow

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)
    val downloadingSongsFlow: Flow<PagingData<SongEntity>> = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getDownloadingSongsAsPagingSource()
    }.flow.cachedIn(viewModelScope)

    private val connection = DownloadServiceConnection()
    private var downloadManager: DownloadManager? = null

    init {
        val intent = Intent(application, DownloadService::class.java)
        application.startService(intent)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private var listeners = mutableListOf<DownloadListener>()
    private val listener: DownloadListener = { task ->
        listeners.forEach { it(task) }
    }

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
}