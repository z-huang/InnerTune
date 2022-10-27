package com.zionhuang.music.ui.listeners

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.WatchPlaylistEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.extensions.show
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.dialogs.EditPlaylistDialog
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IPlaylistMenuListener {
    fun edit(playlist: Playlist)
    fun play(playlists: List<Playlist>)
    fun playNext(playlists: List<Playlist>)
    fun addToQueue(playlists: List<Playlist>)
    fun addToPlaylist(playlists: List<Playlist>)
    fun download(playlists: List<Playlist>)
    fun share(playlist: Playlist)
    fun refetch(playlists: List<Playlist>)
    fun delete(playlists: List<Playlist>)

    fun play(playlist: Playlist) = play(listOf(playlist))
    fun playNext(playlist: Playlist) = playNext(listOf(playlist))
    fun addToQueue(playlist: Playlist) = addToQueue(listOf(playlist))
    fun addToPlaylist(playlist: Playlist) = addToPlaylist(listOf(playlist))
    fun download(playlist: Playlist) = download(listOf(playlist))
    fun refetch(playlist: Playlist) = refetch(listOf(playlist))
    fun delete(playlist: Playlist) = delete(listOf(playlist))
}

class PlaylistMenuListener(override val fragment: Fragment) : BaseMenuListener<Playlist>(fragment), IPlaylistMenuListener {
    private val songRepository by lazy { SongRepository(fragment.requireContext()) }

    override suspend fun getMediaMetadata(items: List<Playlist>): List<MediaMetadata> = withContext(IO) {
        items.flatMap { playlist ->
            if (playlist.playlist.isYouTubePlaylist) {
                YouTube.getQueue(playlistId = playlist.id).getOrThrow().map { it.toMediaMetadata() }
            } else {
                songRepository.getPlaylistSongs(playlist.id).getList().map { it.toMediaMetadata() }
            }
        }
    }

    override fun edit(playlist: Playlist) {
        EditPlaylistDialog().apply {
            arguments = bundleOf(MediaConstants.EXTRA_PLAYLIST to playlist.playlist)
        }.show(context)
    }

    override fun play(playlists: List<Playlist>) {
        if (playlists.size == 1 && playlists[0].playlist.isYouTubePlaylist) {
            NavigationEndpointHandler(fragment).handle(WatchPlaylistEndpoint(playlistId = playlists[0].id))
        } else {
            playAll(if (playlists.size == 1) playlists[0].playlist.name else "", playlists)
        }
    }

    override fun playNext(playlists: List<Playlist>) {
        playNext(playlists, context.resources.getQuantityString(R.plurals.snackbar_playlist_play_next, playlists.size, playlists.size))
    }

    override fun addToQueue(playlists: List<Playlist>) {
        addToQueue(playlists, context.resources.getQuantityString(R.plurals.snackbar_playlist_added_to_queue, playlists.size, playlists.size))
    }

    override fun addToPlaylist(playlists: List<Playlist>) {
        addToPlaylist { playlist ->
            songRepository.addToPlaylist(playlist, playlists)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun download(playlists: List<Playlist>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.downloadPlaylists(playlists)
        }
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
    override fun refetch(playlists: List<Playlist>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.refetchPlaylists(playlists)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(playlists: List<Playlist>) {
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.deletePlaylists(playlists.map { it.playlist })
        }
    }
}