package com.zionhuang.music.utils

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_TEXT
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.extensions.exceptionHandler
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.queues.YouTubeQueue
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.dialogs.ChoosePlaylistDialog
import com.zionhuang.music.ui.fragments.songs.ArtistSongsFragmentArgs
import com.zionhuang.music.ui.fragments.songs.PlaylistSongsFragmentArgs
import com.zionhuang.music.ui.fragments.youtube.YouTubeBrowseFragmentDirections.openYouTubeBrowseFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
open class NavigationEndpointHandler(val fragment: Fragment) {
    val mainActivity: MainActivity
        get() = fragment.requireActivity() as MainActivity
    val context: Context
        get() = fragment.requireContext()
    private val songRepository by lazy { SongRepository(fragment.requireContext()) }

    fun handle(navigationEndpoint: NavigationEndpoint?, item: YTItem? = null) = navigationEndpoint?.endpoint?.let { handle(it, item) }

    fun handle(endpoint: Endpoint, item: YTItem? = null) = when (endpoint) {
        is WatchEndpoint -> {
            MediaSessionConnection.binder?.songPlayer?.playQueue(YouTubeQueue(endpoint, item))
            (fragment.requireActivity() as? MainActivity)?.showBottomSheet()
        }
        is WatchPlaylistEndpoint -> {
            MediaSessionConnection.binder?.songPlayer?.playQueue(YouTubeQueue(endpoint.toWatchEndpoint(), item))
            (fragment.requireActivity() as? MainActivity)?.showBottomSheet()
        }
        is BrowseEndpoint -> {
            fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
            fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
            fragment.findNavController().navigate(openYouTubeBrowseFragment(endpoint))
        }
        is SearchEndpoint -> {}
        is QueueAddEndpoint -> MediaSessionConnection.binder?.songPlayer?.handleQueueAddEndpoint(endpoint, item)
        is ShareEntityEndpoint -> {}
        is BrowseLocalArtistSongsEndpoint -> {
            fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
            fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
            fragment.findNavController().navigate(R.id.artistSongsFragment, ArtistSongsFragmentArgs.Builder(endpoint.artistId).build().toBundle())
        }
    }

    fun share(item: YTItem) {
        val intent = Intent().apply {
            action = ACTION_SEND
            type = "text/plain"
            putExtra(EXTRA_TEXT, item.shareLink)
        }
        fragment.startActivity(Intent.createChooser(intent, null))
    }

    fun playNext(item: YTItem) {
        val mainContent = mainActivity.binding.mainContent
        handle(item.menu.playNextEndpoint, item)
        Snackbar.make(mainContent, fragment.resources.getQuantityString(when (item) {
            is SongItem -> R.plurals.snackbar_song_play_next
            is AlbumItem -> R.plurals.snackbar_album_play_next
            is PlaylistItem -> R.plurals.snackbar_playlist_play_next
            else -> throw UnsupportedOperationException()
        }, 1, 1), LENGTH_SHORT).show()
    }

    fun addToQueue(item: YTItem) {
        val mainContent = mainActivity.binding.mainContent
        handle(item.menu.addToQueueEndpoint, item)
        Snackbar.make(mainContent, fragment.resources.getQuantityString(when (item) {
            is SongItem -> R.plurals.snackbar_song_added_to_queue
            is AlbumItem -> R.plurals.snackbar_album_added_to_queue
            is PlaylistItem -> R.plurals.snackbar_playlist_added_to_queue
            else -> throw UnsupportedOperationException()
        }, 1, 1), LENGTH_SHORT).show()
    }

    fun addToLibrary(item: YTItem) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(context.exceptionHandler) {
            when (item) {
                is SongItem -> songRepository.safeAddSong(item)
                is AlbumItem -> songRepository.addAlbum(item)
                is PlaylistItem -> songRepository.addPlaylist(item)
                else -> {}
            }
            Snackbar.make(mainContent, R.string.snackbar_added_to_library, LENGTH_SHORT).show()
        }
    }

    fun importPlaylist(playlist: PlaylistItem) {
        val mainContent = mainActivity.binding.mainContent
        GlobalScope.launch(context.exceptionHandler) {
            songRepository.importPlaylist(playlist)
            Snackbar.make(mainContent, R.string.snackbar_playlist_imported, LENGTH_SHORT).show()
        }
    }

    fun addToPlaylist(item: YTItem) {
        val mainContent = mainActivity.binding.mainContent
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch(context.exceptionHandler) {
                songRepository.addYouTubeItemToPlaylist(playlist, item)
                Snackbar.make(mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
            }
        }.show(fragment.childFragmentManager, null)
    }
}