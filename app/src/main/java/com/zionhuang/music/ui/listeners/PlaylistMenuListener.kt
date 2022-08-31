package com.zionhuang.music.ui.listeners

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.NavigationEndpoint
import com.zionhuang.innertube.models.QueueAddEndpoint
import com.zionhuang.innertube.models.QueueAddEndpoint.Companion.INSERT_AFTER_CURRENT_VIDEO
import com.zionhuang.innertube.models.QueueAddEndpoint.Companion.INSERT_AT_END
import com.zionhuang.innertube.models.WatchPlaylistEndpoint
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA_ITEMS
import com.zionhuang.music.constants.MediaSessionConstants
import com.zionhuang.music.constants.MediaSessionConstants.COMMAND_PLAY_NEXT
import com.zionhuang.music.db.entities.Playlist
import com.zionhuang.music.extensions.show
import com.zionhuang.music.extensions.toMediaItem
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.ListQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.dialogs.EditPlaylistDialog
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import com.zionhuang.music.utils.NavigationEndpointHandler
import kotlinx.coroutines.*

interface IPlaylistMenuListener {
    fun edit(playlist: Playlist)
    fun play(playlists: List<Playlist>)
    fun playNext(playlists: List<Playlist>)
    fun addToQueue(playlists: List<Playlist>)
    fun addToPlaylist(playlists: List<Playlist>)
    fun share(playlist: Playlist)
    fun refetch(playlists: List<Playlist>)
    fun delete(playlists: List<Playlist>)

    fun play(playlist: Playlist) = play(listOf(playlist))
    fun playNext(playlist: Playlist) = playNext(listOf(playlist))
    fun addToQueue(playlist: Playlist) = addToQueue(listOf(playlist))
    fun addToPlaylist(playlist: Playlist) = addToPlaylist(listOf(playlist))
    fun refetch(playlist: Playlist) = refetch(listOf(playlist))
    fun delete(playlist: Playlist) = delete(listOf(playlist))
}

class PlaylistMenuListener(private val fragment: Fragment) : IPlaylistMenuListener {
    val context: Context
        get() = fragment.requireContext()

    val mainActivity: MainActivity
        get() = fragment.requireActivity() as MainActivity

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
                    SongRepository.getPlaylistSongs(playlist.id, SongSortInfoPreference).getList().map { it.toMediaItem() }
                }
            }
            withContext(Dispatchers.Main) {
                MediaSessionConnection.binder?.songPlayer?.playQueue(ListQueue(
                    items = songs
                ))
            }
        }
    }

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
                    SongRepository.getPlaylistSongs(playlist.id, SongSortInfoPreference).getList().map { it.toMediaMetadata() }
                }
            }
            MediaSessionConnection.mediaController?.sendCommand(
                COMMAND_PLAY_NEXT,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.toTypedArray()),
                null
            )
            Snackbar.make(mainActivity.binding.mainContent, context.resources.getQuantityString(R.plurals.snackbar_playlist_play_next, playlists.size, playlists.size), LENGTH_SHORT).show()
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
            Snackbar.make(mainActivity.binding.mainContent, context.resources.getQuantityString(R.plurals.snackbar_playlist_play_next, 1, 1), LENGTH_SHORT).show()
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
                    SongRepository.getPlaylistSongs(playlist.id, SongSortInfoPreference).getList().map { it.toMediaMetadata() }
                }
            }
            MediaSessionConnection.mediaController?.sendCommand(
                MediaSessionConstants.COMMAND_ADD_TO_QUEUE,
                bundleOf(EXTRA_MEDIA_METADATA_ITEMS to songs.toTypedArray()),
                null
            )
            Snackbar.make(mainActivity.binding.mainContent, context.resources.getQuantityString(R.plurals.snackbar_playlist_added_to_queue, playlists.size, playlists.size), LENGTH_SHORT).show()
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
            Snackbar.make(mainActivity.binding.mainContent, context.resources.getQuantityString(R.plurals.snackbar_playlist_added_to_queue, 1, 1), LENGTH_SHORT).show()
        } else {
            addToQueue(listOf(playlist))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addToPlaylist(playlists: List<Playlist>) {
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch {
                SongRepository.addToPlaylist(playlist, playlists)
                Snackbar.make(mainActivity.binding.mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
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
    override fun refetch(playlists: List<Playlist>) {
        GlobalScope.launch {
            playlists.forEach { playlist ->
                SongRepository.refetchPlaylist(playlist)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete(playlists: List<Playlist>) {
        GlobalScope.launch {
            SongRepository.deletePlaylists(playlists.map { it.playlist })
        }
    }
}