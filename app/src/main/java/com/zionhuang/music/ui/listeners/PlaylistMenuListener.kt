package com.zionhuang.music.ui.listeners

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.innertube.models.QueueAddEndpoint
import com.zionhuang.innertube.models.QueueAddEndpoint.Companion.INSERT_AFTER_CURRENT_VIDEO
import com.zionhuang.innertube.models.QueueAddEndpoint.Companion.INSERT_AT_END
import com.zionhuang.innertube.models.WatchPlaylistEndpoint
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaSessionConstants
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.extensions.show
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.PreferenceSortInfo
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.dialogs.EditPlaylistDialog
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.*

interface IPlaylistMenuListener {
    fun edit(playlist: Playlist)
    fun play(playlists: List<Playlist>)
    fun playNext(playlists: List<Playlist>)
    fun addToQueue(playlists: List<Playlist>)
    fun addToPlaylist(playlists: List<Playlist>)
    fun share(playlist: Playlist)
    fun delete(playlists: List<Playlist>)

    fun play(playlist: Playlist) = play(listOf(playlist))
    fun playNext(playlist: Playlist) = playNext(listOf(playlist))
    fun addToQueue(playlist: Playlist) = addToQueue(listOf(playlist))
    fun addToPlaylist(playlist: Playlist) = addToPlaylist(listOf(playlist))
    fun delete(playlist: Playlist) = delete(listOf(playlist))
}

class PlaylistMenuListener(private val fragment: Fragment) : IPlaylistMenuListener {
    val context: Context
        get() = fragment.requireContext()

    override fun edit(playlist: Playlist) {
        EditPlaylistDialog().apply {
            arguments = bundleOf(MediaConstants.EXTRA_PLAYLIST to playlist)
        }.show(context)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun play(playlists: List<Playlist>) {
        GlobalScope.launch {
            val songs = playlists.flatMap { playlist ->
                if (playlist.playlist.isYouTubePlaylist) {
                    YouTube.getQueue(playlistId = playlist.id).map { it.toMediaItem() }
                } else {
                    SongRepository.getPlaylistSongs(playlist.id, PreferenceSortInfo).getList().map { it.toMediaItem() }
                }
            }
            withContext(Dispatchers.Main) {
                MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                    items = songs
                ))
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun play(playlist: Playlist) {
        if (playlist.playlist.isYouTubePlaylist) {
            NavigationEndpointHandler(fragment).handle(NavigationEndpoint(
                watchPlaylistEndpoint = WatchPlaylistEndpoint(playlistId = playlist.id)
            ))
        } else {
            play(listOf(playlist))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun playNext(playlists: List<Playlist>) {
        GlobalScope.launch {
            val songs = playlists.flatMap { playlist ->
                if (playlist.playlist.isYouTubePlaylist) {
                    YouTube.getQueue(playlistId = playlist.id).map { it.toMediaMetadata() }
                } else {
                    SongRepository.getPlaylistSongs(playlist.id, PreferenceSortInfo).getList().map { it.toMediaMetadata() }
                }
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.toTypedArray()),
                null
            )
        }
    }

    override fun playNext(playlist: Playlist) {
        if (playlist.playlist.isYouTubePlaylist) {
            NavigationEndpointHandler(fragment).handle(NavigationEndpoint(
                queueAddEndpoint = QueueAddEndpoint(
                    queueInsertPosition = INSERT_AFTER_CURRENT_VIDEO,
                    queueTarget = QueueAddEndpoint.QueueTarget(
                        playlistId = playlist.id
                    )
                )
            ))
        } else {
            playNext(listOf(playlist))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToQueue(playlists: List<Playlist>) {
        GlobalScope.launch {
            val songs = playlists.flatMap { playlist ->
                if (playlist.playlist.isYouTubePlaylist) {
                    YouTube.getQueue(playlistId = playlist.id).map { it.toMediaMetadata() }
                } else {
                    SongRepository.getPlaylistSongs(playlist.id, PreferenceSortInfo).getList().map { it.toMediaMetadata() }
                }
            }
            MediaSessionConnection.mediaController?.sendCommand(
                MediaSessionConstants.COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.toTypedArray()),
                null
            )
        }
    }

    override fun addToQueue(playlist: Playlist) {
        if (playlist.playlist.isYouTubePlaylist) {
            NavigationEndpointHandler(fragment).handle(NavigationEndpoint(
                queueAddEndpoint = QueueAddEndpoint(
                    queueInsertPosition = INSERT_AT_END,
                    queueTarget = QueueAddEndpoint.QueueTarget(
                        playlistId = playlist.id
                    )
                )
            ))
        } else {
            addToQueue(listOf(playlist))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(playlists: List<Playlist>) {
        ChoosePlaylistDialog {
            GlobalScope.launch {
                SongRepository.addToPlaylist(it, playlists)
            }
        }.show(fragment.childFragmentManager, null)
    }

    override fun share(playlist: Playlist) {
        if (playlist.playlist.isYouTubePlaylist) {
            val intent = Intent().apply {
                action = ACTION_SEND
                type = "text/plain"
                putExtra(EXTRA_TEXT, "https://music.youtube.com/playlist?list=${playlist.id}")
            }
            fragment.startActivity(Intent.createChooser(intent, null))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(playlists: List<Playlist>) {
        GlobalScope.launch {
            SongRepository.deletePlaylists(playlists.map { it.playlist })
        }
    }
}