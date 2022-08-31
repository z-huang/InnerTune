package com.zionhuang.music.utils

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

    open fun handle(navigationEndpoint: NavigationEndpoint?, item: YTItem? = null) = when (val endpoint = navigationEndpoint?.endpoint) {
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
        null -> {}
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
        handle(item.menu.playNextEndpoint, item)
        Snackbar.make(mainActivity.binding.mainContent, fragment.resources.getQuantityString(when (item) {
            is SongItem -> R.plurals.snackbar_song_play_next
            is AlbumItem -> R.plurals.snackbar_album_play_next
            is PlaylistItem -> R.plurals.snackbar_playlist_play_next
            else -> throw UnsupportedOperationException()
        }, 1, 1), LENGTH_SHORT).show()
    }

    fun addToQueue(item: YTItem) {
        handle(item.menu.addToQueueEndpoint, item)
        Snackbar.make(mainActivity.binding.mainContent, fragment.resources.getQuantityString(when (item) {
            is SongItem -> R.plurals.snackbar_song_added_to_queue
            is AlbumItem -> R.plurals.snackbar_album_added_to_queue
            is PlaylistItem -> R.plurals.snackbar_playlist_added_to_queue
            else -> throw UnsupportedOperationException()
        }, 1, 1), LENGTH_SHORT).show()
    }

    fun addToLibrary(item: YTItem) {
        GlobalScope.launch {
            when (item) {
                is SongItem -> SongRepository.safeAddSong(item)
                is AlbumItem -> SongRepository.addAlbum(item)
                is PlaylistItem -> SongRepository.addPlaylist(item)
                else -> {}
            }
            Snackbar.make(mainActivity.binding.mainContent, R.string.snackbar_added_to_library, LENGTH_SHORT).show()
        }
    }

    fun importPlaylist(playlist: PlaylistItem) {
        GlobalScope.launch {
            SongRepository.importPlaylist(playlist)
            Snackbar.make(mainActivity.binding.mainContent, R.string.snackbar_playlist_imported, LENGTH_SHORT).show()
        }
    }

    fun addToPlaylist(item: YTItem) {
        ChoosePlaylistDialog { playlist ->
            GlobalScope.launch {
                SongRepository.addToPlaylist(playlist, item)
                Snackbar.make(mainActivity.binding.mainContent, fragment.getString(R.string.snackbar_added_to_playlist, playlist.name), LENGTH_SHORT)
                    .setAction(R.string.snackbar_action_view) {
                        fragment.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(R.id.fragment_content)
                        fragment.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).addTarget(R.id.fragment_content)
                        fragment.findNavController().navigate(R.id.playlistSongsFragment, PlaylistSongsFragmentArgs.Builder(playlist.id).build().toBundle())
                    }.show()
            }
        }.show(fragment.childFragmentManager, null)
    }
}