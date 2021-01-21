package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.HEADER_ITEM_ID
import com.zionhuang.music.constants.ORDER_NAME
import com.zionhuang.music.db.SongRepository
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.ChannelEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.download.DownloadService
import com.zionhuang.music.download.DownloadServiceConnection
import com.zionhuang.music.download.DownloadTask
import com.zionhuang.music.extensions.getActivity
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.songs.SongDetailsDialog
import com.zionhuang.music.ui.listeners.SongPopupMenuListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository: SongRepository = SongRepository(application)

    var sortType by preference(R.string.pref_sort_type, ORDER_NAME)

    val allSongsFlow: Flow<PagingData<Song>> by lazy {
        Pager(PagingConfig(pageSize = 50, enablePlaceholders = true)) {
            songRepository.getAllSongsPagingSource(sortType)
        }.flow.map { pagingData ->
            pagingData.insertHeaderItem(Song(HEADER_ITEM_ID))
        }.cachedIn(viewModelScope)
    }

    val allArtistsFlow: Flow<PagingData<ArtistEntity>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.allArtistsPagingSource
        }.flow.cachedIn(viewModelScope)
    }

    val allChannelsFlow: Flow<PagingData<ChannelEntity>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.allChannelsPagingSource
        }.flow.cachedIn(viewModelScope)
    }

    suspend fun getSong(songId: String) = songRepository.getSongById(songId)

    fun getArtistSongsAsFlow(artistId: Int) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getArtistSongsAsPagingSource(artistId)
    }.flow.cachedIn(viewModelScope)

    fun getChannelSongsAsFlow(channelId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getChannelSongsAsPagingSource(channelId)
    }.flow.map { pagingData ->
        pagingData.insertHeaderItem(Song(HEADER_ITEM_ID))
    }.cachedIn(viewModelScope)

    private val _deleteSong = MutableLiveData<SongEntity>()
    val deleteSong: LiveData<SongEntity>
        get() = _deleteSong
    val songPopupMenuListener = object : SongPopupMenuListener {
        override fun editSong(songId: String, view: View) {
            (view.getActivity() as? MainActivity)?.let { activity ->
                val transition = activity.supportFragmentManager.beginTransaction()
                        .addToBackStack(null)
                SongDetailsDialog(songId).show(transition, "SongDialog")
            }
        }

        override fun downloadSong(songId: String, context: Context) {
            context.startService(Intent(context, DownloadService::class.java).apply {
                action = DownloadService.ACTION_DOWNLOAD_MUSIC
                putExtra("task", DownloadTask(id = songId))
            })
        }

        override fun deleteSong(songId: String) {
            viewModelScope.launch {
                songRepository.getSongEntityById(songId)?.let { song ->
                    songRepository.deleteSong(song)
                    _deleteSong.postValue(song)
                }
            }
        }
    }

    val downloadServiceConnection = DownloadServiceConnection(application)
}