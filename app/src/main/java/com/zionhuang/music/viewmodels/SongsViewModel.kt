package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.download.DownloadListener
import com.zionhuang.music.download.DownloadManager
import com.zionhuang.music.download.DownloadService
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.ui.fragments.LibraryFragmentDirections
import com.zionhuang.music.ui.listeners.SongPopupMenuListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)

    val allSongsFlow: Flow<PagingData<SongEntity>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllSongsAsPagingSource()
        }.flow.cachedIn(viewModelScope)
    }

    val downloadingSongsFlow: Flow<PagingData<SongEntity>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getDownloadingSongsAsPagingSource()
        }.flow.cachedIn(viewModelScope)
    }

    val songPopupMenuListener = object : SongPopupMenuListener {
        override fun editSong(songId: String, view: View) {
            view.findNavController().navigate(LibraryFragmentDirections.actionLibraryFragmentToSongDetailsFragment(songId))
        }

        override fun downloadSong(songId: String, context: Context) {
            context.startService(Intent(context, DownloadService::class.java).apply {
                action = DownloadService.DOWNLOAD_MUSIC_INTENT
                putExtra("task", DownloadTask(id = songId))
            })
        }

        override fun deleteSong(song: SongEntity) {
            viewModelScope.launch {
                songRepository.deleteSong(song)
            }
        }
    }

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
        val intent = Intent(application, DownloadService::class.java)
        application.startService(intent)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
}