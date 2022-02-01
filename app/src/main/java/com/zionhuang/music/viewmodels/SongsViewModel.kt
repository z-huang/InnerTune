package com.zionhuang.music.viewmodels

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import androidx.paging.*
import androidx.paging.TerminalSeparatorType.FULLY_COMPLETE
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.HEADER_ITEM_ID
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONG
import com.zionhuang.music.constants.MediaConstants.EXTRA_SONGS
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_ADD_TO_QUEUE
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.models.PreferenceSortInfo
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.DownloadProgress
import com.zionhuang.music.models.toMediaData
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.EditSongDialog
import com.zionhuang.music.ui.listeners.SongPopupMenuListener
import com.zionhuang.music.ui.listeners.StreamPopupMenuListener
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import kotlin.collections.set

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository: LocalRepository = SongRepository
    val mediaSessionConnection = MediaSessionConnection

    val sortInfo: IMutableSortInfo = PreferenceSortInfo

    var query: String? = null

    val allSongsFlow: Flow<PagingData<Song>> by lazy {
        Pager(PagingConfig(pageSize = 50, enablePlaceholders = true)) {
            if (!query.isNullOrBlank()) {
                songRepository.searchSongs(query!!).pagingSource
            } else {
                songRepository.getAllSongs(sortInfo).pagingSource
            }
        }.flow.map { pagingData ->
            if (query.isNullOrBlank()) pagingData.insertHeaderItem(FULLY_COMPLETE, Song(HEADER_ITEM_ID))
            else pagingData
        }.cachedIn(viewModelScope)
    }

    val allArtistsFlow: Flow<PagingData<ArtistEntity>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllArtists().pagingSource
        }.flow.cachedIn(viewModelScope)
    }

    val allPlaylistsFlow: Flow<PagingData<PlaylistEntity>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllPlaylists().pagingSource
        }.flow.cachedIn(viewModelScope)
    }

    fun getArtistSongsAsFlow(artistId: Int) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getArtistSongs(artistId, sortInfo).pagingSource
    }.flow.map { pagingData ->
        pagingData.insertHeaderItem(FULLY_COMPLETE, Song(HEADER_ITEM_ID))
    }.cachedIn(viewModelScope)

    fun getPlaylistSongsAsFlow(playlistId: Int) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getPlaylistSongs(playlistId, sortInfo).pagingSource
    }.flow.cachedIn(viewModelScope)

    val songPopupMenuListener = object : SongPopupMenuListener {
        override fun editSong(song: Song, context: Context) {
            (context.getActivity() as? MainActivity)?.let { activity ->
                EditSongDialog().apply {
                    arguments = bundleOf(EXTRA_SONG to song)
                }.show(activity.supportFragmentManager, EditSongDialog.TAG)
            }
        }

        override fun playNext(songs: List<Song>, context: Context) {
            mediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_SONGS to songs.map { it.toMediaData(context) }.toTypedArray()),
                null
            )
        }

        override fun addToQueue(songs: List<Song>, context: Context) {
            mediaSessionConnection.mediaController?.sendCommand(
                COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_SONGS to songs.map { it.toMediaData(context) }.toTypedArray()),
                null
            )
        }

        override fun addToPlaylist(songs: List<Song>, context: Context) {
            viewModelScope.launch {
                val playlists = songRepository.getAllPlaylists().getList()
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.dialog_choose_playlist_title)
                    .setItems(playlists.map { it.name }.toTypedArray()) { _, i ->
                        viewModelScope.launch {
                            songRepository.addSongsToPlaylist(playlists[i].playlistId, songs)
                        }
                    }
                    .show()
            }
        }

        override fun downloadSongs(songIds: List<String>, context: Context) {
            viewModelScope.launch {
                songRepository.downloadSongs(songIds)
            }
        }

        override fun deleteSongs(songs: List<Song>) {
            viewModelScope.launch {
                songRepository.deleteSongs(songs)
            }
        }
    }

    val streamPopupMenuListener = object : StreamPopupMenuListener {
        override fun addToLibrary(songs: List<StreamInfoItem>) {
            viewModelScope.launch {
                songRepository.addSongs(songs.map(StreamInfoItem::toSong))
            }
        }

        override fun playNext(songs: List<StreamInfoItem>) {
            mediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_SONGS to songs.map(StreamInfoItem::toMediaData).toTypedArray()),
                null
            )
        }

        override fun addToQueue(songs: List<StreamInfoItem>) {
            mediaSessionConnection.mediaController?.sendCommand(
                COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_SONGS to songs.map(StreamInfoItem::toMediaData).toTypedArray()),
                null
            )
        }

        override fun addToPlaylist(songs: List<StreamInfoItem>, context: Context) {
            viewModelScope.launch {
                val playlists = songRepository.getAllPlaylists().getList()
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.dialog_choose_playlist_title)
                    .setItems(playlists.map { it.name }.toTypedArray()) { _, i ->
                        viewModelScope.launch {
                            songs.map(StreamInfoItem::toSong).let {
                                songRepository.addSongs(it)
                                songRepository.addSongsToPlaylist(playlists[i].playlistId, it)
                            }
                        }
                    }
                    .show()
            }
        }

        override fun download(songs: List<StreamInfoItem>, context: Context) {
            viewModelScope.launch {
                songs.map(StreamInfoItem::toSong).let { s ->
                    songRepository.addSongs(s)
                    songRepository.downloadSongs(s.map { it.songId })
                }
            }
        }
    }

    val downloadInfoLiveData = liveData(viewModelScope.coroutineContext + IO) {
        songRepository.getAllDownloads().flow.collectLatest { list ->
            list.associateBy { it.id }.let { map ->
                while (true) {
                    emit(if (list.isEmpty()) emptyMap() else getDownloadInfo(*list.map { it.id }
                        .toLongArray()).mapKeys { map[it.key]!!.songId })
                    delay(500)
                }
            }
        }
    }

    private val downloadManager = application.getSystemService<DownloadManager>()!!

    private fun getDownloadInfo(vararg ids: Long): Map<Long, DownloadProgress> {
        val res = mutableMapOf<Long, DownloadProgress>()
        downloadManager.query(DownloadManager.Query().setFilterById(*ids)).forEach {
            res[get(DownloadManager.COLUMN_ID)] = DownloadProgress(
                status = get(DownloadManager.COLUMN_STATUS),
                currentBytes = get(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR),
                totalBytes = get(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )
        }
        return res
    }
}
